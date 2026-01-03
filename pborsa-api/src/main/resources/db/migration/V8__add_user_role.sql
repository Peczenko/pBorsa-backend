-- Add role column to users table for RBAC support
ALTER TABLE users
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Create index for faster role-based queries
CREATE INDEX idx_users_role ON users(role);

-- Add comment for documentation
COMMENT ON COLUMN users.role IS 'User role for authorization: USER or ADMIN';


