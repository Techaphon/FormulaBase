-- Keep user references and production history when an account is removed.
ALTER TABLE users ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
