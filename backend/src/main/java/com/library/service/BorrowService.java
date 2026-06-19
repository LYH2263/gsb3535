package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.domain.Book;
import com.library.domain.BorrowRecord;
import com.library.domain.LibraryUser;
import com.library.dto.BorrowRequest;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.LibraryUserMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BorrowService {
    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final LibraryUserMapper userMapper;

    public BorrowService(BorrowRecordMapper borrowRecordMapper, BookMapper bookMapper, LibraryUserMapper userMapper) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
    }

    public List<BorrowRecord> listAll() {
        return populateDetails(borrowRecordMapper.selectList(new LambdaQueryWrapper<BorrowRecord>().orderByAsc(BorrowRecord::getDueDate)));
    }

    public List<BorrowRecord> listByUser(Long userId) {
        return populateDetails(borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .eq(BorrowRecord::getUserId, userId)
                        .orderByAsc(BorrowRecord::getDueDate)
        ));
    }

    public List<BorrowRecord> listHistory() {
        return populateDetails(borrowRecordMapper.selectList(new LambdaQueryWrapper<BorrowRecord>().orderByAsc(BorrowRecord::getDueDate)));
    }

    public List<BorrowRecord> listOverdue() {
        return populateDetails(borrowRecordMapper.selectList(
                new LambdaQueryWrapper<BorrowRecord>()
                        .isNull(BorrowRecord::getReturnDate)
                        .lt(BorrowRecord::getDueDate, LocalDateTime.now())
                        .orderByAsc(BorrowRecord::getDueDate)
        ));
    }

    public BorrowRecord getById(Long id) {
        return populateDetails(borrowRecordMapper.selectById(id));
    }

    @Transactional
    public BorrowRecord borrow(BorrowRequest request) {
        // 校验：同一用户不允许重复借阅同一本未归还的书
        Long count = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, request.getUserId())
                .eq(BorrowRecord::getBookId, request.getBookId())
                .isNull(BorrowRecord::getReturnDate));
        if (count > 0) {
            throw new RuntimeException("您已借阅该书且尚未归还，不可重复借阅");
        }

        Book book = bookMapper.selectById(request.getBookId());
        if (book == null || book.getAvailableCopies() <= 0) {
            return null;
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookMapper.updateById(book);

        BorrowRecord record = new BorrowRecord();
        record.setBookId(request.getBookId());
        record.setUserId(request.getUserId());
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(request.getBorrowDays()));
        record.setStatus("BORROWED");
        borrowRecordMapper.insert(record);
        return populateDetails(record);
    }

    @Transactional
    public BorrowRecord returnBook(Long borrowId) {
        BorrowRecord record = borrowRecordMapper.selectById(borrowId);
        if (record == null || record.getReturnDate() != null) {
            return null;
        }
        record.setReturnDate(LocalDateTime.now());
        record.setStatus("RETURNED");
        borrowRecordMapper.updateById(record);

        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookMapper.updateById(book);
        }
        return populateDetails(record);
    }

    private List<BorrowRecord> populateDetails(List<BorrowRecord> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }
        Set<Long> bookIds = records.stream().map(BorrowRecord::getBookId).collect(Collectors.toSet());
        Set<Long> userIds = records.stream().map(BorrowRecord::getUserId).collect(Collectors.toSet());

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

    private BorrowRecord populateDetails(BorrowRecord record) {
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
