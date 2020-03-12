-- 1.安裝 MySQL Community
-- https://dev.mysql.com/downloads/mysql/

-- 2.下載 ConnectJ 8.x
-- https://dev.mysql.com/downloads/connector/j/

-- 3.透過 Netbeans 連線 MySQL -> mysql 資料庫
-- jdbc:mysql://localhost:3306/mysql?useUnicode=true

-- 4.建立 hs 資料庫
-- CREATE DATABASE hs CHARACTER SET utf8 COLLATE utf8_general_ci;

-- 5.透過 Netbeans 連線 MySQL -> hs 資料庫
-- jdbc:mysql://localhost:3306/hs?useUnicode=true

-- 6.依序建立資料表
-- 員工資料表
CREATE TABLE Employee(
    emp_id int NOT NULL AUTO_INCREMENT, -- 主鍵(Employee序號)
    emp_no varchar(10) not null UNIQUE, -- (員工編號)
    emp_name varchar(50) not null, -- (員工姓名)
    emp_active boolean not null, -- (在線員工?)
    emp_ct timestamp default current_timestamp, -- (建檔時間)
    emp_rfid varchar(50), -- (RFID)
    PRIMARY KEY (emp_id)
);

-- 打卡狀態表
CREATE TABLE Status(
    status_id int NOT NULL AUTO_INCREMENT, -- 主鍵(序號)
    status_no int not null UNIQUE , -- (打卡狀態號碼)
    status_name varchar(10) not null UNIQUE, -- (打卡狀態名稱)
    status_begin int,
    status_end int,
    PRIMARY KEY (status_id)
);

-- 打卡記錄
CREATE TABLE ClockOn(
    clock_id int NOT NULL AUTO_INCREMENT, -- 主鍵(序號)
    emp_id int,
    status_id int,
    clock_on timestamp default current_timestamp, -- (打卡時間)
    image LONGTEXT,
    PRIMARY KEY (clock_id),
    FOREIGN KEY (emp_id) REFERENCES Employee(emp_id), -- 外鍵(Employee序號)
    FOREIGN KEY (status_id) REFERENCES Status(status_id) -- 外鍵(Status序號)
);

