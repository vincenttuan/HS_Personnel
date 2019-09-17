-- 員工資料表
CREATE TABLE Employee(
    emp_id integer primary key generated always as identity, -- 主鍵(Employee序號)
    emp_no varchar(10) not null UNIQUE, -- (員工編號)
    emp_name varchar(50) not null, -- (員工姓名)
    emp_active boolean not null, -- (在線員工?)
    emp_ct timestamp default current_timestamp -- (建檔時間)
);

-- 打卡狀態表
CREATE TABLE Status(
    status_id integer primary key generated always as identity, -- 主鍵(序號)
    status_no integer not null UNIQUE , -- (打卡狀態號碼)
    status_name varchar(10) not null UNIQUE -- (打卡狀態名稱)
);

-- 打卡記錄
CREATE TABLE ClockOn(
    clock_id integer primary key generated always as identity, -- 主鍵(序號)
    emp_id integer constraint emp_id_fk references Employee(emp_id),-- 外鍵(Employee序號)
    status_id integer constraint status_id_fk references Status(status_id), -- 外鍵(Status序號)
    clock_on timestamp default current_timestamp -- (打卡時間)
);

-- 查詢員工上下班紀錄
SELECT s.STATUS_NAME, e.EMP_NAME, c.CLOCK_ON 
FROM APP.EMPLOYEE e, APP.CLOCKON c, APP.STATUS s
WHERE e.EMP_ID = c.EMP_ID and c.STATUS_ID in (1, 2) and s.STATUS_ID = c.STATUS_ID

-- 在 CLOCKON 中增加 image 欄位
ALTER TABLE APP.CLOCKON ADD COLUMN image CLOB;

-- 在 Status 中增加時段欄位
ALTER TABLE APP.Status ADD status_begin int;
ALTER TABLE APP.Status ADD status_end int;

-- 在 Employee 中增加 rfid 卡號欄位
ALTER TABLE APP.Employee ADD emp_rfid varchar(50);

-- 根據 EMP_NO 與 指定打卡日期 查找打卡資料
Select e.emp_no, e.emp_name, s.status_name, c.clock_on, c.image
From employee e, status s, clockon c
Where e.EMP_NO = '0011' and e.EMP_ID = c.EMP_ID and c.STATUS_ID = s.STATUS_ID
and c.CLOCK_ON >= '2019-08-22 00:00:00'
Order by clock_on

-- 根據 EMP_NO 與 指定打卡區間日期 查找打卡資料
Select e.emp_no, e.emp_name, s.status_name, c.clock_on, c.image
From employee e, status s, clockon c
Where e.EMP_NO = '0011' and e.EMP_ID = c.EMP_ID and c.STATUS_ID = s.STATUS_ID
and c.CLOCK_ON Between '2019-08-12 00:00:00' and '2019-08-22 23:59:59'
Order by clock_on