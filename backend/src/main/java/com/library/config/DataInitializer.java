package com.library.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.domain.Book;
import com.library.domain.BorrowRecord;
import com.library.domain.LibraryUser;
import com.library.dto.UserRequest;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.LibraryUserMapper;
import com.library.service.UserService;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserService userService;
    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final LibraryUserMapper userMapper;

    public DataInitializer(UserService userService, BookMapper bookMapper, BorrowRecordMapper borrowRecordMapper, LibraryUserMapper userMapper) {
        this.userService = userService;
        this.bookMapper = bookMapper;
        this.borrowRecordMapper = borrowRecordMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userMapper.selectCount(new LambdaQueryWrapper<>()) == 0) {
            UserRequest admin = new UserRequest();
            admin.setUsername("admin");
            admin.setFullName("系统管理员");
            admin.setRole("ADMIN");
            userService.create(admin, "123456");

            UserRequest user = new UserRequest();
            user.setUsername("reader");
            user.setFullName("示例读者");
            user.setRole("USER");
            userService.create(user, "123456");
        }

        if (bookMapper.selectCount(new LambdaQueryWrapper<>()) == 0) {
            String[] titles = {
                "云原生架构实践", "未来城市的设计逻辑", "数据驱动产品运营",
                "深度学习入门", "微服务设计指南", "领域驱动设计详解",
                "敏捷软件开发原则", "代码整洁之道", "重构：改善既有代码",
                "高性能MySQL", "算法导论", "计算机网络：自顶向下",
                "操作系统概念"
            };
            String[] authors = {
                "李晓峰", "王韵", "赵宁", "张三", "李四", "王五",
                "赵六", "孙七", "周八", "吴九", "郑十", "冯十一", "陈十二"
            };
            String[] categories = {
                "技术", "人文", "管理", "技术", "技术", "技术",
                "管理", "技术", "技术", "数据库", "算法", "网络", "系统"
            };

            for (int i = 0; i < titles.length; i++) {
                Book book = new Book();
                book.setTitle(titles[i]);
                book.setAuthor(authors[i]);
                book.setIsbn("ISBN-1000" + (i + 1));
                book.setCategory(categories[i]);
                book.setTotalCopies(5 + (i % 3));
                book.setAvailableCopies(book.getTotalCopies());
                if (i == 2) { // 保持之前的初始借阅逻辑
                    book.setAvailableCopies(book.getTotalCopies() - 2);
                }
                bookMapper.insert(book);
            }
        }

        if (borrowRecordMapper.selectCount(new LambdaQueryWrapper<>()) == 0) {
            LibraryUser reader = userMapper.selectOne(new LambdaQueryWrapper<LibraryUser>().eq(LibraryUser::getUsername, "reader"));
            Book book = bookMapper.selectOne(new LambdaQueryWrapper<Book>().eq(Book::getIsbn, "ISBN-10003"));
            if (reader != null && book != null) {
                BorrowRecord record = new BorrowRecord();
                record.setUserId(reader.getId());
                record.setBookId(book.getId());
                record.setBorrowDate(LocalDateTime.now().minusDays(10));
                record.setDueDate(LocalDateTime.now().minusDays(2));
                record.setStatus("BORROWED");
                borrowRecordMapper.insert(record);
            }
        }
    }
}
