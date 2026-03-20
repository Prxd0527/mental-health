INSERT INTO post (id, user_id, content, is_anonymous, likes, views, status) VALUES (1, 1, '这是一个测试树洞', 1, 0, 0, 1) ON DUPLICATE KEY UPDATE content='这是一个测试树洞';
INSERT INTO comment (id, post_id, user_id, content, is_anonymous, likes, status) VALUES (1, 1, 1, '加油！', 1, 0, 1) ON DUPLICATE KEY UPDATE content='加油！';
