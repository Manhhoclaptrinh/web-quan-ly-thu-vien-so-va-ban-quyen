

DROP DATABASE IF EXISTS `cnj25_library`;
CREATE DATABASE `cnj25_library` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `cnj25_library`;

SET FOREIGN_KEY_CHECKS = 0;

-- ==================== users ====================
CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,      -- lưu mật khẩu đã hash (SHA-256/BCrypt)
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) UNIQUE,
    role          ENUM('ADMIN','LIBRARIAN','READER') NOT NULL DEFAULT 'READER',
    status        ENUM('ACTIVE','LOCKED') NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==================== categories ====================
CREATE TABLE categories (
    category_id   INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    description   VARCHAR(255)
);

-- ==================== documents ====================
CREATE TABLE documents (
    document_id   INT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    author        VARCHAR(150),
    category_id   INT,
    file_path     VARCHAR(255) NOT NULL,      -- đường dẫn file lưu trên server
    description   TEXT,
    upload_date   DATETIME DEFAULT CURRENT_TIMESTAMP,
    uploaded_by   INT,                        -- user_id người upload
    access_level  ENUM('PUBLIC','RESTRICTED','PRIVATE') NOT NULL DEFAULT 'PUBLIC',
    status        ENUM('AVAILABLE','DISABLED') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_doc_category FOREIGN KEY (category_id) REFERENCES categories(category_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_doc_uploader FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
        ON DELETE SET NULL
);

-- ==================== videos ====================
CREATE TABLE videos (
    video_id      INT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    author        VARCHAR(150),
    category_id   INT,
    file_path     VARCHAR(255) NOT NULL,      -- đường dẫn file video lưu trên server
    duration_seconds INT,                     -- thời lượng video (giây), có thể null nếu chưa xác định
    thumbnail_path VARCHAR(255),              -- ảnh đại diện (tùy chọn)
    description   TEXT,
    upload_date   DATETIME DEFAULT CURRENT_TIMESTAMP,
    uploaded_by   INT,
    access_level  ENUM('PUBLIC','RESTRICTED','PRIVATE') NOT NULL DEFAULT 'PUBLIC',
    status        ENUM('AVAILABLE','DISABLED') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_video_category FOREIGN KEY (category_id) REFERENCES categories(category_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_video_uploader FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
        ON DELETE SET NULL
);

-- ==================== licenses ====================
CREATE TABLE licenses (
    license_id     INT AUTO_INCREMENT PRIMARY KEY,
    document_id    INT NOT NULL,
    license_type   ENUM('FREE','SUBSCRIPTION','PAID','INTERNAL') NOT NULL DEFAULT 'FREE',
    license_code   VARCHAR(100),
    issued_date    DATE,
    expiry_date    DATE,
    terms          TEXT,
    status         ENUM('VALID','EXPIRED','REVOKED') NOT NULL DEFAULT 'VALID',
    CONSTRAINT fk_license_document FOREIGN KEY (document_id) REFERENCES documents(document_id)
        ON DELETE CASCADE
);

-- ==================== permissions ====================
CREATE TABLE permissions (
    permission_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    document_id    INT NOT NULL,
    permission_type ENUM('VIEW','DOWNLOAD','EDIT') NOT NULL DEFAULT 'VIEW',
    granted_by     INT,                       -- user_id admin/librarian cấp quyền
    granted_date   DATETIME DEFAULT CURRENT_TIMESTAMP,
    expiry_date    DATETIME NULL,
    CONSTRAINT fk_perm_user FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_perm_document FOREIGN KEY (document_id) REFERENCES documents(document_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_perm_grantor FOREIGN KEY (granted_by) REFERENCES users(user_id)
        ON DELETE SET NULL,
    UNIQUE KEY uq_user_doc_perm (user_id, document_id, permission_type)
);

-- ==================== access_history ====================
CREATE TABLE access_history (
    history_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    document_id    INT NULL,      -- NULL khi hành động không gắn với tài liệu cụ thể (LOGIN/LOGOUT)
    target_type   ENUM('DOCUMENT','VIDEO') NOT NULL DEFAULT 'DOCUMENT',
    video_id      INT NULL,
    action_type    ENUM('VIEW','DOWNLOAD','LOGIN','LOGOUT') NOT NULL,
    access_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
    ip_address     VARCHAR(45),
    CONSTRAINT fk_hist_user FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_hist_document FOREIGN KEY (document_id) REFERENCES documents(document_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_hist_video FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE
);

-- ==================== permission_requests ====================
CREATE TABLE permission_requests (
    request_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL,
    document_id      INT NOT NULL,
    permission_type  ENUM('VIEW','DOWNLOAD') NOT NULL DEFAULT 'VIEW',
    reason           VARCHAR(500),
    status           ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    requested_date   DATETIME DEFAULT CURRENT_TIMESTAMP,
    processed_date   DATETIME NULL,
    processed_by     INT NULL,
    expiry_date      DATETIME NULL,
    CONSTRAINT fk_req_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_req_document FOREIGN KEY (document_id) REFERENCES documents(document_id) ON DELETE CASCADE,
    CONSTRAINT fk_req_processor FOREIGN KEY (processed_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_req_status (status),
    INDEX idx_req_user (user_id),
    INDEX idx_req_document (document_id)
);

-- ==================== favorites ====================
CREATE TABLE favorites (
    favorite_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_type ENUM('DOCUMENT','VIDEO') NOT NULL DEFAULT 'DOCUMENT',
    document_id INT NULL,
    video_id INT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_fav_document FOREIGN KEY (document_id) REFERENCES documents(document_id) ON DELETE CASCADE,
    CONSTRAINT fk_fav_video FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
    UNIQUE KEY uq_favorite_user_document (user_id, document_id),
    UNIQUE KEY uq_favorite_user_video (user_id, video_id),
    INDEX idx_fav_user (user_id)
);

-- ==================== document_reviews ====================
CREATE TABLE document_reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    document_id INT NOT NULL,
    rating TINYINT NOT NULL,
    comment VARCHAR(1000),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_document FOREIGN KEY (document_id) REFERENCES documents(document_id) ON DELETE CASCADE,
    UNIQUE KEY uq_review_user_document (user_id, document_id),
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
    INDEX idx_review_document (document_id)
);

-- ==================== notifications ====================
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(50) DEFAULT 'SYSTEM',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_notification_user_read (user_id, is_read)
);

-- ==================== audit_logs ====================
CREATE TABLE audit_logs (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    action_type VARCHAR(60) NOT NULL,
    target_type VARCHAR(60),
    target_id INT NULL,
    description VARCHAR(1000),
    ip_address VARCHAR(45),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_audit_created (created_at),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action_type)
);

-- ==================== video_licenses ====================
CREATE TABLE video_licenses (
    license_id     INT AUTO_INCREMENT PRIMARY KEY,
    video_id       INT NOT NULL,
    license_type   ENUM('FREE','SUBSCRIPTION','PAID','INTERNAL') NOT NULL DEFAULT 'FREE',
    license_code   VARCHAR(100),
    issued_date    DATE,
    expiry_date    DATE,
    terms          TEXT,
    status         ENUM('VALID','EXPIRED','REVOKED') NOT NULL DEFAULT 'VALID',
    CONSTRAINT fk_vlicense_video FOREIGN KEY (video_id) REFERENCES videos(video_id)
        ON DELETE CASCADE
);

-- ==================== video_permissions ====================
CREATE TABLE video_permissions (
    permission_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    video_id       INT NOT NULL,
    permission_type ENUM('VIEW','DOWNLOAD','EDIT') NOT NULL DEFAULT 'VIEW',
    granted_by     INT,
    granted_date   DATETIME DEFAULT CURRENT_TIMESTAMP,
    expiry_date    DATETIME NULL,
    CONSTRAINT fk_vperm_user FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_vperm_video FOREIGN KEY (video_id) REFERENCES videos(video_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_vperm_grantor FOREIGN KEY (granted_by) REFERENCES users(user_id)
        ON DELETE SET NULL,
    UNIQUE KEY uq_user_video_perm (user_id, video_id, permission_type)
);

-- ==================== video_permission_requests ====================
CREATE TABLE video_permission_requests (
    request_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL,
    video_id         INT NOT NULL,
    permission_type  ENUM('VIEW','DOWNLOAD') NOT NULL DEFAULT 'VIEW',
    reason           VARCHAR(500),
    status           ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    requested_date   DATETIME DEFAULT CURRENT_TIMESTAMP,
    processed_date   DATETIME NULL,
    processed_by     INT NULL,
    expiry_date      DATETIME NULL,
    CONSTRAINT fk_vreq_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_vreq_video FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
    CONSTRAINT fk_vreq_processor FOREIGN KEY (processed_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_vreq_status (status),
    INDEX idx_vreq_user (user_id),
    INDEX idx_vreq_video (video_id)
);

-- ==================== video_reviews ====================
CREATE TABLE video_reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    video_id INT NOT NULL,
    rating TINYINT NOT NULL,
    comment VARCHAR(1000),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_vreview_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_vreview_video FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
    UNIQUE KEY uq_review_user_video (user_id, video_id),
    CONSTRAINT chk_vreview_rating CHECK (rating BETWEEN 1 AND 5),
    INDEX idx_vreview_video (video_id)
);

-- =====================================================
-- DỮ LIỆU
-- =====================================================

-- Dữ liệu bảng users
INSERT INTO users (username, password, full_name, email, role) VALUES
('admin',   '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Quản trị viên', 'admin@eaut.edu.vn', 'ADMIN'),
('librarian','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Thủ thư A', 'librarian@eaut.edu.vn', 'LIBRARIAN'),
('reader1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Nguyễn Văn A', 'reader1@eaut.edu.vn', 'READER');

-- Dữ liệu bảng categories
INSERT INTO categories (category_name, description) VALUES
('Công nghệ thông tin', 'Sách và tài liệu về CNTT'),
('Kinh tế', 'Sách và tài liệu về kinh tế, quản trị'),
('Ngoại ngữ', 'Tài liệu học ngoại ngữ');

-- Dữ liệu bảng documents
INSERT INTO documents (title, author, category_id, file_path, description, uploaded_by, access_level) VALUES
('Giáo trình Java cơ bản', 'Nguyễn Văn B', 1, '/uploads/java-co-ban.pdf', 'Tài liệu học lập trình Java', 1, 'PUBLIC'),
('Phân tích thiết kế hệ thống', 'Trần Thị C', 1, '/uploads/pttk-he-thong.pdf', 'Giáo trình PTTK hệ thống thông tin', 1, 'RESTRICTED'),
('Nguyên lý kinh tế học', 'Lê Văn D', 2, '/uploads/kinh-te-hoc.pdf', 'Sách kinh tế học cơ bản', 2, 'PUBLIC');

-- Dữ liệu bảng videos
INSERT INTO videos (title, author, category_id, file_path, description, uploaded_by, access_level) VALUES
('Video hướng dẫn Java cơ bản', 'Nguyễn Văn B', 1, '/WEB-INF/uploads/videos/sample-java.mp4', 'Video bài giảng nhập môn Java', 1, 'PUBLIC'),
('Video PTTK hệ thống (nội bộ)', 'Trần Thị C', 1, '/WEB-INF/uploads/videos/sample-pttk.mp4', 'Video bài giảng PTTK hệ thống, lưu hành nội bộ', 1, 'RESTRICTED');

-- Dữ liệu bảng licenses
INSERT INTO licenses (document_id, license_type, license_code, issued_date, expiry_date, terms) VALUES
(1, 'FREE', 'LIC-0001', '2025-01-01', NULL, 'Được phép sử dụng miễn phí cho mục đích học tập'),
(2, 'INTERNAL', 'LIC-0002', '2025-01-01', '2026-12-31', 'Chỉ lưu hành nội bộ trường EAUT'),
(3, 'FREE', 'LIC-0003', '2025-01-01', NULL, 'Được phép sử dụng miễn phí');

-- Dữ liệu bảng permissions
INSERT INTO permissions (user_id, document_id, permission_type, granted_by) VALUES
(3, 1, 'VIEW', 1),
(3, 1, 'DOWNLOAD', 1),
(3, 2, 'VIEW', 2);

-- Dữ liệu bảng video_licenses
INSERT INTO video_licenses (video_id, license_type, license_code, issued_date, expiry_date, terms) VALUES
(1, 'FREE', 'VLIC-0001', '2025-01-01', NULL, 'Được phép sử dụng miễn phí cho mục đích học tập'),
(2, 'INTERNAL', 'VLIC-0002', '2025-01-01', '2026-12-31', 'Chỉ lưu hành nội bộ trường EAUT');

SET FOREIGN_KEY_CHECKS = 1;

-- Hoàn tất.
