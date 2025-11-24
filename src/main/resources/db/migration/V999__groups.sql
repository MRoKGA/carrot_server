-- Flyway migration for group feature
CREATE TABLE IF NOT EXISTS community_group (
  id INT AUTO_INCREMENT PRIMARY KEY,
  owner_id INT NOT NULL,
  region_id INT NOT NULL,
  name VARCHAR(40) NOT NULL,
  description VARCHAR(500) NOT NULL,
  cover_image_url VARCHAR(255),
  visibility VARCHAR(20) NOT NULL,
  join_policy VARCHAR(20) NOT NULL,
  max_members INT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NULL,
  CONSTRAINT fk_group_owner FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT fk_group_region FOREIGN KEY (region_id) REFERENCES region(id)
);

CREATE TABLE IF NOT EXISTS group_membership (
  id INT AUTO_INCREMENT PRIMARY KEY,
  group_id INT NOT NULL,
  user_id INT NOT NULL,
  role VARCHAR(20) NOT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uq_group_user (group_id, user_id),
  CONSTRAINT fk_mem_group FOREIGN KEY (group_id) REFERENCES groups(id),
  CONSTRAINT fk_mem_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS group_join_request (
  id INT AUTO_INCREMENT PRIMARY KEY,
  group_id INT NOT NULL,
  user_id INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  message VARCHAR(200),
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_req_group FOREIGN KEY (group_id) REFERENCES groups(id),
  CONSTRAINT fk_req_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS group_post (
  id INT AUTO_INCREMENT PRIMARY KEY,
  group_id INT NOT NULL,
  author_id INT NOT NULL,
  type VARCHAR(20) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  image_url VARCHAR(255),
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_post_group FOREIGN KEY (group_id) REFERENCES groups(id),
  CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS group_event (
  id INT AUTO_INCREMENT PRIMARY KEY,
  group_id INT NOT NULL,
  creator_id INT NOT NULL,
  title VARCHAR(60) NOT NULL,
  description VARCHAR(400) NOT NULL,
  latitude DOUBLE NOT NULL,
  longitude DOUBLE NOT NULL,
  name VARCHAR(100) NOT NULL,
  start_at DATETIME NOT NULL,
  end_at DATETIME NOT NULL,
  capacity INT,
  fee INT,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_event_group FOREIGN KEY (group_id) REFERENCES groups(id),
  CONSTRAINT fk_event_creator FOREIGN KEY (creator_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS group_event_rsvp (
  id INT AUTO_INCREMENT PRIMARY KEY,
  event_id INT NOT NULL,
  user_id INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uq_event_user (event_id, user_id),
  CONSTRAINT fk_rsvp_event FOREIGN KEY (event_id) REFERENCES group_event(id),
  CONSTRAINT fk_rsvp_user FOREIGN KEY (user_id) REFERENCES users(id)
);
