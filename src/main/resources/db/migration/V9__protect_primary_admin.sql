ALTER TABLE users ADD COLUMN primary_admin BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE users SET primary_admin = TRUE WHERE id = (
 SELECT id FROM users WHERE role = 'CEO' AND active = TRUE AND archived = FALSE
 ORDER BY CASE WHEN lower(username) = 'ceo' THEN 0 ELSE 1 END, username, id LIMIT 1
);
