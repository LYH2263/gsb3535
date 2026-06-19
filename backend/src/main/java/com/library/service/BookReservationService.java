package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    public IPage<BookReservation> listPage(int current, int size, String keyword) {
        Page<BookReservation> page = new Page<>(current, size);
        LambdaQueryWrapper<BookReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(BookReservation::getQueuePosition);
        queryWrapper.orderByDesc(BookReservation::getReservationTime);
        
        IPage<BookReservation> result = reservationMapper.selectPage(page, queryWrapper);
        result = populateDetails(result);
        
        if (StringUtils.hasText(keyword)) {
            List<BookReservation> filtered = result.getRecords().stream()
                    .filter(r -> {
                        String bookTitle = r.getBookTitle() != null ? r.getBookTitle() : "";
                        String userName = r.getUserName() != null ? r.getUserName() : "";
                        return bookTitle.contains(keyword) || userName.contains(keyword);
                    })
                    .collect(Collectors.toList());
            result.setRecords(filtered);
            result.setTotal(filtered.size());
        }
        
        return result;
    }

    public List<BookReservation> listByUser(Long userId) {
        List<BookReservation> records = reservationMapper.selectList(
                new LambdaQueryWrapper<BookReservation>()
                        .eq(BookReservation::getUserId, userId)
                        .orderByAsc(BookReservation::getQueuePosition)
        );
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
            throw new RuntimeException("该书当前可借数量大于0，可直接借阅，无需预约");
        }

        Long existingCount = reservationMapper.selectCount(new LambdaQueryWrapper<BookReservation>()
                .eq(BookReservation::getUserId, request.getUserId())
                .eq(BookReservation::getBookId, request.getBookId())
                .eq(BookReservation::getStatus, "WAITING"));
        if (existingCount > 0) {
            throw new RuntimeException("您已对该书进行预约，请勿重复预约");
        }

        Long queueCount = reservationMapper.selectCount(new LambdaQueryWrapper<BookReservation>()
                .eq(BookReservation::getBookId, request.getBookId())
                .eq(BookReservation::getStatus, "WAITING"));

        BookReservation reservation = new BookReservation();
        reservation.setUserId(request.getUserId());
        reservation.setBookId(request.getBookId());
        reservation.setQueuePosition(queueCount.intValue() + 1);
        reservation.setReservationTime(LocalDateTime.now());
        reservation.setStatus("WAITING");
        reservationMapper.insert(reservation);
        return populateDetails(reservation);
    }

    @Transactional
    public boolean cancel(Long id) {
        BookReservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            return false;
        }
        if (!"WAITING".equals(reservation.getStatus())) {
            throw new RuntimeException("只能取消排队中的预约");
        }

        Long bookId = reservation.getBookId();
        int canceledPosition = reservation.getQueuePosition();

        reservation.setStatus("CANCELLED");
        reservationMapper.updateById(reservation);

        List<BookReservation> waitingList = reservationMapper.selectList(
                new LambdaQueryWrapper<BookReservation>()
                        .eq(BookReservation::getBookId, bookId)
                        .eq(BookReservation::getStatus, "WAITING")
                        .gt(BookReservation::getQueuePosition, canceledPosition)
                        .orderByAsc(BookReservation::getQueuePosition)
        );
        for (BookReservation r : waitingList) {
            r.setQueuePosition(r.getQueuePosition() - 1);
            reservationMapper.updateById(r);
        }
        return true;
    }

    private IPage<BookReservation> populateDetails(IPage<BookReservation> page) {
        page.setRecords(populateDetails(page.getRecords()));
        return page;
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
