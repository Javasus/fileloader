USE fileloader;

CREATE TABLE IF NOT EXISTS users
(
    id   INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS files
    (
        id INT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(255) NOT NULL,
        file_path VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS events
(
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    file_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE
);

CREATE INDEX idx_events_user ON events(user_id);
CREATE INDEX idx_events_file ON events(file_id);