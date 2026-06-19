CREATE TABLE IF NOT EXISTS library_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS book (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  author VARCHAR(100) NOT NULL,
  isbn VARCHAR(40) NOT NULL UNIQUE,
  category VARCHAR(50),
  total_copies INT NOT NULL,
  available_copies INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS borrow_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  borrow_date DATETIME NOT NULL,
  due_date DATETIME NOT NULL,
  return_date DATETIME,
  status VARCHAR(20) NOT NULL,
  CONSTRAINT fk_borrow_user FOREIGN KEY (user_id) REFERENCES library_user(id),
  CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES book(id)
);

CREATE TABLE IF NOT EXISTS book_reservation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  queue_position INT NOT NULL,
  reservation_time DATETIME NOT NULL,
  status VARCHAR(20) NOT NULL,
  CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES library_user(id),
  CONSTRAINT fk_reservation_book FOREIGN KEY (book_id) REFERENCES book(id)
);
