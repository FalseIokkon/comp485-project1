PRAGMA foreign_keys = ON;

-- =========================
-- ORGANIZATION
-- =========================

CREATE TABLE IF NOT EXISTS departments (
    department_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS branches (
    branch_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    city TEXT NOT NULL,
    state TEXT NOT NULL
);

-- =========================
-- HR
-- =========================

CREATE TABLE IF NOT EXISTS employees (
    employee_id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    job_title TEXT NOT NULL,
    department_id INTEGER NOT NULL,
    branch_id INTEGER NOT NULL,
    hire_date TEXT NOT NULL,
    employment_status TEXT NOT NULL CHECK (
        employment_status IN ('ACTIVE', 'ON_LEAVE', 'TERMINATED')
    ),
    performance_rating REAL DEFAULT 3.0 CHECK (
        performance_rating >= 1.0 AND performance_rating <= 5.0
    ),
    attendance_rate REAL DEFAULT 100.0 CHECK (
        attendance_rate >= 0.0 AND attendance_rate <= 100.0
    ),
    salary REAL NOT NULL CHECK (salary >= 0),
    FOREIGN KEY (department_id) REFERENCES departments(department_id),
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
);

CREATE TABLE IF NOT EXISTS job_openings (
    opening_id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    department_id INTEGER NOT NULL,
    branch_id INTEGER NOT NULL,
    posted_date TEXT NOT NULL,
    status TEXT NOT NULL CHECK (
        status IN ('OPEN', 'FILLED', 'CLOSED')
    ),
    FOREIGN KEY (department_id) REFERENCES departments(department_id),
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
);

-- =========================
-- FINANCE
-- =========================

CREATE TABLE IF NOT EXISTS invoices (
    invoice_id INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_number TEXT NOT NULL UNIQUE,
    customer_id INTEGER NOT NULL,
    invoice_date TEXT NOT NULL,
    due_date TEXT NOT NULL,
    amount REAL NOT NULL CHECK (amount >= 0),
    amount_paid REAL NOT NULL DEFAULT 0 CHECK (amount_paid >= 0),
    payment_status TEXT NOT NULL CHECK (
        payment_status IN ('UNPAID', 'PARTIAL', 'PAID', 'OVERDUE')
    )
);

CREATE TABLE IF NOT EXISTS expenses (
    expense_id INTEGER PRIMARY KEY AUTOINCREMENT,
    expense_date TEXT NOT NULL,
    department_id INTEGER,
    category TEXT NOT NULL,
    description TEXT,
    amount REAL NOT NULL CHECK (amount >= 0),
    FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

CREATE TABLE IF NOT EXISTS payroll (
    payroll_id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    pay_period_start TEXT NOT NULL,
    pay_period_end TEXT NOT NULL,
    gross_pay REAL NOT NULL CHECK (gross_pay >= 0),
    bonus REAL NOT NULL DEFAULT 0 CHECK (bonus >= 0),
    deductions REAL NOT NULL DEFAULT 0 CHECK (deductions >= 0),
    net_pay REAL NOT NULL CHECK (net_pay >= 0),
    paid_date TEXT NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

-- =========================
-- MARKETING
-- =========================

CREATE TABLE IF NOT EXISTS campaigns (
    campaign_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    channel TEXT NOT NULL,
    budget REAL NOT NULL CHECK (budget >= 0),
    spend REAL NOT NULL DEFAULT 0 CHECK (spend >= 0),
    start_date TEXT NOT NULL,
    end_date TEXT,
    status TEXT NOT NULL CHECK (
        status IN ('PLANNED', 'ACTIVE', 'COMPLETED', 'PAUSED')
    )
);

CREATE TABLE IF NOT EXISTS leads (
    lead_id INTEGER PRIMARY KEY AUTOINCREMENT,
    campaign_id INTEGER NOT NULL,
    lead_date TEXT NOT NULL,
    source TEXT NOT NULL,
    status TEXT NOT NULL CHECK (
        status IN ('NEW', 'CONTACTED', 'QUALIFIED', 'CONVERTED', 'LOST')
    ),
    customer_id INTEGER,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id)
);

-- =========================
-- OPERATIONS / MANUFACTURING
-- =========================

CREATE TABLE IF NOT EXISTS products (
    product_id INTEGER PRIMARY KEY AUTOINCREMENT,
    sku TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    unit_price REAL NOT NULL CHECK (unit_price >= 0),
    unit_cost REAL NOT NULL CHECK (unit_cost >= 0),
    is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))
);

CREATE TABLE IF NOT EXISTS production_batches (
    batch_id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    scheduled_date TEXT NOT NULL,
    completed_date TEXT,
    units_planned INTEGER NOT NULL CHECK (units_planned >= 0),
    units_produced INTEGER NOT NULL DEFAULT 0 CHECK (units_produced >= 0),
    defect_count INTEGER NOT NULL DEFAULT 0 CHECK (defect_count >= 0),
    machine_uptime REAL DEFAULT 100.0 CHECK (
        machine_uptime >= 0.0 AND machine_uptime <= 100.0
    ),
    status TEXT NOT NULL CHECK (
        status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'DELAYED')
    ),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- =========================
-- LOGISTICS / DISTRIBUTING
-- =========================

CREATE TABLE IF NOT EXISTS customers (
    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    company_name TEXT NOT NULL,
    contact_name TEXT,
    city TEXT NOT NULL,
    state TEXT NOT NULL,
    status TEXT NOT NULL CHECK (
        status IN ('ACTIVE', 'PROSPECT', 'INACTIVE')
    )
);

CREATE TABLE IF NOT EXISTS orders (
    order_id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    order_date TEXT NOT NULL,
    total_amount REAL NOT NULL CHECK (total_amount >= 0),
    status TEXT NOT NULL CHECK (
        status IN ('PENDING', 'APPROVED', 'SHIPPED', 'DELIVERED', 'CANCELLED')
    ),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE IF NOT EXISTS shipments (
    shipment_id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL UNIQUE,
    destination_city TEXT NOT NULL,
    destination_state TEXT NOT NULL,
    ship_date TEXT NOT NULL,
    delivery_date TEXT,
    delivery_status TEXT NOT NULL CHECK (
        delivery_status IN ('READY', 'IN_TRANSIT', 'DELIVERED', 'DELAYED', 'FAILED')
    ),
    on_time INTEGER NOT NULL DEFAULT 1 CHECK (on_time IN (0, 1)),
    delivery_days INTEGER CHECK (delivery_days >= 0),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- =========================
-- LATE FOREIGN KEYS
-- =========================

-- invoices.customer_id references customers, so customers must exist first.
-- SQLite does not support adding constraints later with ALTER TABLE easily,
-- so we define it by recreating the intended relationship through usage.
-- For this project, customer_id should still be populated with valid IDs.

-- leads.customer_id is optional and should reference customers when present.

-- =========================
-- HELPFUL INDEXES
-- =========================

CREATE INDEX IF NOT EXISTS idx_employees_department_id
ON employees(department_id);

CREATE INDEX IF NOT EXISTS idx_employees_branch_id
ON employees(branch_id);

CREATE INDEX IF NOT EXISTS idx_job_openings_department_id
ON job_openings(department_id);

CREATE INDEX IF NOT EXISTS idx_invoices_invoice_date
ON invoices(invoice_date);

CREATE INDEX IF NOT EXISTS idx_expenses_expense_date
ON expenses(expense_date);

CREATE INDEX IF NOT EXISTS idx_payroll_paid_date
ON payroll(paid_date);

CREATE INDEX IF NOT EXISTS idx_campaigns_status
ON campaigns(status);

CREATE INDEX IF NOT EXISTS idx_leads_lead_date
ON leads(lead_date);

CREATE INDEX IF NOT EXISTS idx_production_batches_scheduled_date
ON production_batches(scheduled_date);

CREATE INDEX IF NOT EXISTS idx_orders_order_date
ON orders(order_date);

CREATE INDEX IF NOT EXISTS idx_shipments_ship_date
ON shipments(ship_date);



CREATE TABLE IF NOT EXISTS division_kpi (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    division TEXT NOT NULL,
    metric TEXT NOT NULL,
    value REAL NOT NULL,
    as_of TEXT NOT NULL
);