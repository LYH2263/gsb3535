package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.domain.Book;
import com.library.dto.BookRequest;
import com.library.mapper.BookMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BookService {
    private final BookMapper bookMapper;

    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public List<Book> listAll() {
        return bookMapper.selectList(new LambdaQueryWrapper<>());
    }

    public IPage<Book> listPage(int current, int size, String keyword) {
        Page<Book> page = new Page<>(current, size);
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Book::getTitle, keyword)
                    .or()
                    .like(Book::getAuthor, keyword)
                    .or()
                    .like(Book::getIsbn, keyword);
        }
        queryWrapper.orderByDesc(Book::getId);
        return bookMapper.selectPage(page, queryWrapper);
    }

    public Book getById(Long id) {
        return bookMapper.selectById(id);
    }

    @Transactional
    public Book create(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(request.getTotalCopies());
        bookMapper.insert(book);
        return book;
    }

    @Transactional
    public Book update(Long id, BookRequest request) {
        Book existing = bookMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        existing.setTitle(request.getTitle());
        existing.setAuthor(request.getAuthor());
        existing.setIsbn(request.getIsbn());
        existing.setCategory(request.getCategory());
        int delta = request.getTotalCopies() - existing.getTotalCopies();
        existing.setTotalCopies(request.getTotalCopies());
        int newAvailable = existing.getAvailableCopies() + delta;
        existing.setAvailableCopies(Math.max(newAvailable, 0));
        bookMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public boolean delete(Long id) {
        return bookMapper.deleteById(id) > 0;
    }
}
