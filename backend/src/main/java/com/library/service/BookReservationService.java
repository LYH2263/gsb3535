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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BookReservationService {
    public static final String STATUS_WAITING = "WAITING";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_FULFILLED = "FULFILLED";

    private final BookReservationMapper reservationMapper;
    private final BookMapper bookMapper;
    private final LibraryUserMapper userMapper;

    public BookReservationService(BookReservationMapper reservationMapper,
                                  BookMapper bookMapper,
                                  LibraryUserMapper userMapper) {
        this.reservationMapper = reservationMapper;
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
    }

    public List<BookReservation> listAll(String bookKeyword, String userKeyword) {
        List<BookReservation> all = reservationMapper.selectList(
                new LambdaQueryWrapper<BookReservation>()
                        .orderByAsc(BookReservation::getBookId)
                        .orderByAsc(BookReservation::getReservedAt)
        );
        List<BookReservation> populated = populateDetails(all);
        if (populated == null) {
            return new ArrayList<>();
        }
        return populated.stream().filter(r -> {
            boolean matchBook = !StringUtils.hasText(bookKeyword)
                    || (r.getBookTitle() != null && r.getBookTitle().toLowerCase().contains(bookKeyword.toLowerCase()));
            boolean matchUser = !StringUtils.hasText(userKeyword)
                    || (r.getUserName() != null && r.getUserName().toLowerCase().contains(userKeyword.toLowerCase()));
            return matchBook && matchUser;
        }).collect(Collectors.toList());
    }

    public List<BookReservation> listByUser(Long userId) {
        List<BookReservation> records = reservationMapper.selectList(
                new LambdaQueryWrapper<BookReservation>()
                        .eq(BookReservation::getUserId, userId)
                        .orderByDesc(BookReservation::getReservedAt)
        );
        return populateDetails(records);
    }

    public BookReservation getById(Long id) {
        BookReservation r = reservationMapper.selectById(id);
        if (r == null) {
            return null;
        }
        return populateDetails(List.of(r)).get(0);
    }

    @Transactional
    public BookReservation reserve(BookReservationRequest request) {
        Book book = bookMapper.selectById(request.getBookId());
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getAvailableCopies() != null && book.getAvailableCopies() > 0) {
            throw new RuntimeException("当前可借数量充足，请直接借阅，无需预约");
        }
        LibraryUser user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new RuntimeException("读者不存在");
        }

        Long existing = reservationMapper.selectCount(
                new LambdaQueryWrapper<BookReservation>()
                        .eq(BookReservation::getUserId, request.getUserId())
                        .eq(BookReservation::getBookId, request.getBookId())
                        .eq(BookReservation::getStatus, STATUS_WAITING)
        );
        if (existing != null && existing > 0) {
            throw new RuntimeException("您已对该图书发起预约，请勿重复操作");
        }

        BookReservation reservation = new BookReservation();
        reservation.setUserId(request.getUserId());
        reservation.setBookId(request.getBookId());
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setStatus(STATUS_WAITING);
        reservationMapper.insert(reservation);
        return populateDetails(List.of(reservation)).get(0);
    }

    @Transactional
    public BookReservation cancel(Long id) {
        BookReservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        if (!STATUS_WAITING.equals(reservation.getStatus())) {
            throw new RuntimeException("仅排队中的预约可取消");
        }
        reservation.setStatus(STATUS_CANCELLED);
        reservationMapper.updateById(reservation);
        return populateDetails(List.of(reservation)).get(0);
    }

    private List<BookReservation> populateDetails(List<BookReservation> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }
        Set<Long> bookIds = records.stream().map(BookReservation::getBookId).collect(Collectors.toSet());
        Set<Long> userIds = records.stream().map(BookReservation::getUserId).collect(Collectors.toSet());

        Map<Long, String> bookTitles = bookIds.isEmpty() ? new HashMap<>() :
                bookMapper.selectBatchIds(bookIds).stream()
                        .collect(Collectors.toMap(Book::getId, Book::getTitle));
        Map<Long, String> userNames = userIds.isEmpty() ? new HashMap<>() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(LibraryUser::getId, LibraryUser::getFullName));

        Map<Long, List<BookReservation>> waitingByBook = reservationMapper.selectList(
                new LambdaQueryWrapper<BookReservation>()
                        .eq(BookReservation::getStatus, STATUS_WAITING)
                        .in(BookReservation::getBookId, bookIds)
                        .orderByAsc(BookReservation::getReservedAt)
        ).stream().collect(Collectors.groupingBy(BookReservation::getBookId));

        records.forEach(r -> {
            r.setBookTitle(bookTitles.getOrDefault(r.getBookId(), "未知图书"));
            r.setUserName(userNames.getOrDefault(r.getUserId(), "未知读者"));
            if (STATUS_WAITING.equals(r.getStatus())) {
                List<BookReservation> queue = waitingByBook.getOrDefault(r.getBookId(), List.of());
                int idx = 0;
                for (int i = 0; i < queue.size(); i++) {
                    if (queue.get(i).getId().equals(r.getId())) {
                        idx = i + 1;
                        break;
                    }
                }
                r.setQueuePosition(idx);
            } else {
                r.setQueuePosition(0);
            }
        });
        return records;
    }
}
