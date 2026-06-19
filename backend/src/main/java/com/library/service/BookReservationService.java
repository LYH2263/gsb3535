package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.domain.Book;
import com.library.domain.BookReservation;
import com.library.domain.LibraryUser;
import com.library.dto.ReservationRequest;
import com.library.mapper.BookMapper;
import com.library.mapper.BookReservationMapper;
import com.library.mapper.LibraryUserMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BookReservationService {
    private final BookReservationMapper reservationMapper;
    private final BookMapper bookMapper;
    private final LibraryUserMapper userMapper;

    public BookReservationService(BookReservationMapper reservationMapper, BookMapper bookMapper, LibraryUserMapper userMapper) {
        this.reservationMapper = reservationMapper;
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
    }

    public List<BookReservation> listByUser(Long userId) {
        List<BookReservation> records = reservationMapper.selectList(
                new LambdaQueryWrapper<BookReservation>()
                        .eq(BookReservation::getUserId, userId)
                        .orderByDesc(BookReservation::getReservationTime)
        );
        return populateDetails(records);
    }

    public List<BookReservation> listAll(String keyword) {
        LambdaQueryWrapper<BookReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(BookReservation::getReservationTime);

        List<BookReservation> records = reservationMapper.selectList(queryWrapper);

        if (StringUtils.hasText(keyword)) {
            records = populateDetails(records);
            final String kw = keyword.toLowerCase();
            records = records.stream()
                    .filter(r -> (r.getBookTitle() != null && r.getBookTitle().toLowerCase().contains(kw))
                            || (r.getUserName() != null && r.getUserName().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
            return records;
        }
        return populateDetails(records);
    }

    public BookReservation getById(Long id) {
        return populateDetails(reservationMapper.selectById(id));
    }

    @Transactional
    public BookReservation create(ReservationRequest request) {
        Book book = bookMapper.selectById(request.getBookId());
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getAvailableCopies() > 0) {
            throw new RuntimeException("该图书当前可借数大于0，可直接借阅，无需预约");
        }

        Long duplicateCount = reservationMapper.selectCount(new LambdaQueryWrapper<BookReservation>()
                .eq(BookReservation::getUserId, request.getUserId())
                .eq(BookReservation::getBookId, request.getBookId())
                .eq(BookReservation::getStatus, "WAITING"));
        if (duplicateCount > 0) {
            throw new RuntimeException("您已对该书进行预约，请勿重复预约");
        }

        Long waitingCount = reservationMapper.selectCount(new LambdaQueryWrapper<BookReservation>()
                .eq(BookReservation::getBookId, request.getBookId())
                .eq(BookReservation::getStatus, "WAITING"));

        BookReservation reservation = new BookReservation();
        reservation.setUserId(request.getUserId());
        reservation.setBookId(request.getBookId());
        reservation.setQueuePosition(waitingCount.intValue() + 1);
        reservation.setReservationTime(LocalDateTime.now());
        reservation.setStatus("WAITING");
        reservationMapper.insert(reservation);
        return populateDetails(reservation);
    }

    @Transactional
    public BookReservation cancel(Long reservationId) {
        BookReservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        if (!"WAITING".equals(reservation.getStatus())) {
            throw new RuntimeException("仅待处理状态的预约可以取消");
        }

        int canceledPosition = reservation.getQueuePosition();
        Long bookId = reservation.getBookId();

        reservation.setStatus("CANCELLED");
        reservation.setCancelTime(LocalDateTime.now());
        reservationMapper.updateById(reservation);

        List<BookReservation> waitingList = reservationMapper.selectList(
                new LambdaQueryWrapper<BookReservation>()
                        .eq(BookReservation::getBookId, bookId)
                        .eq(BookReservation::getStatus, "WAITING")
                        .orderByAsc(BookReservation::getQueuePosition)
        );
        for (BookReservation r : waitingList) {
            if (r.getQueuePosition() > canceledPosition) {
                r.setQueuePosition(r.getQueuePosition() - 1);
                reservationMapper.updateById(r);
            }
        }

        return populateDetails(reservation);
    }

    private List<BookReservation> populateDetails(List<BookReservation> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }
        Set<Long> bookIds = records.stream().map(BookReservation::getBookId).collect(Collectors.toSet());
        Set<Long> userIds = records.stream().map(BookReservation::getUserId).collect(Collectors.toSet());

        Map<Long, String> bookTitles = bookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Book::getTitle));
        Map<Long, String> userNames = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(LibraryUser::getId, LibraryUser::getFullName));

        records.forEach(r -> {
            r.setBookTitle(bookTitles.getOrDefault(r.getBookId(), "Unknown Book"));
            r.setUserName(userNames.getOrDefault(r.getUserId(), "Unknown User"));
        });
        return records;
    }

    private BookReservation populateDetails(BookReservation record) {
        if (record == null) {
            return null;
        }
        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            record.setBookTitle(book.getTitle());
        }
        LibraryUser user = userMapper.selectById(record.getUserId());
        if (user != null) {
            record.setUserName(user.getFullName());
        }
        return record;
    }
}
