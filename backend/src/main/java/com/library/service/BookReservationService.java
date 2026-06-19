package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.domain.Book;
import com.library.domain.BookReservation;
import com.library.domain.LibraryUser;
import com.library.dto.BookReservationRequest;
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

    public List<BookReservation> listAll(String keyword) {
        LambdaQueryWrapper<BookReservation> wrapper = new LambdaQueryWrapper<BookReservation>()
                .orderByAsc(BookReservation::getQueuePosition);

        List<BookReservation> reservations = reservationMapper.selectList(wrapper);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            reservations = populateDetails(reservations).stream()
                    .filter(r -> (r.getBookTitle() != null && r.getBookTitle().toLowerCase().contains(kw))
                            || (r.getUserName() != null && r.getUserName().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
            return reservations;
        }

        return populateDetails(reservations);
    }

    public List<BookReservation> listByUser(Long userId) {
        List<BookReservation> reservations = reservationMapper.selectList(
                new LambdaQueryWrapper<BookReservation>()
                        .eq(BookReservation::getUserId, userId)
                        .orderByAsc(BookReservation::getQueuePosition)
        );
        return populateDetails(reservations);
    }

    @Transactional
    public BookReservation reserve(BookReservationRequest request) {
        Book book = bookMapper.selectById(request.getBookId());
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getAvailableCopies() > 0) {
            throw new RuntimeException("该图书当前有可借库存，可直接借阅");
        }

        Long existingCount = reservationMapper.selectCount(new LambdaQueryWrapper<BookReservation>()
                .eq(BookReservation::getUserId, request.getUserId())
                .eq(BookReservation::getBookId, request.getBookId())
                .eq(BookReservation::getStatus, "WAITING"));
        if (existingCount > 0) {
            throw new RuntimeException("您已预约该书，请勿重复预约");
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
    public BookReservation cancel(Long reservationId) {
        BookReservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        if (!"WAITING".equals(reservation.getStatus())) {
            throw new RuntimeException("该预约已处理，无法取消");
        }

        reservation.setStatus("CANCELLED");
        reservationMapper.updateById(reservation);

        List<BookReservation> waitingList = reservationMapper.selectList(
                new LambdaQueryWrapper<BookReservation>()
                        .eq(BookReservation::getBookId, reservation.getBookId())
                        .eq(BookReservation::getStatus, "WAITING")
                        .gt(BookReservation::getQueuePosition, reservation.getQueuePosition())
                        .orderByAsc(BookReservation::getQueuePosition)
        );

        for (int i = 0; i < waitingList.size(); i++) {
            BookReservation r = waitingList.get(i);
            r.setQueuePosition(reservation.getQueuePosition() + i);
            reservationMapper.updateById(r);
        }

        return populateDetails(reservation);
    }

    private List<BookReservation> populateDetails(List<BookReservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return reservations;
        }
        Set<Long> bookIds = reservations.stream().map(BookReservation::getBookId).collect(Collectors.toSet());
        Set<Long> userIds = reservations.stream().map(BookReservation::getUserId).collect(Collectors.toSet());

        Map<Long, String> bookTitles = bookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Book::getTitle));
        Map<Long, String> userNames = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(LibraryUser::getId, LibraryUser::getFullName));

        reservations.forEach(r -> {
            r.setBookTitle(bookTitles.getOrDefault(r.getBookId(), "Unknown Book"));
            r.setUserName(userNames.getOrDefault(r.getUserId(), "Unknown User"));
        });
        return reservations;
    }

    private BookReservation populateDetails(BookReservation reservation) {
        if (reservation == null) {
            return null;
        }
        Book book = bookMapper.selectById(reservation.getBookId());
        if (book != null) {
            reservation.setBookTitle(book.getTitle());
        }
        LibraryUser user = userMapper.selectById(reservation.getUserId());
        if (user != null) {
            reservation.setUserName(user.getFullName());
        }
        return reservation;
    }
}
