UPDATE user SET password = (SELECT p.password FROM (SELECT password FROM user WHERE username='test_admin') AS p) WHERE id > 0;
