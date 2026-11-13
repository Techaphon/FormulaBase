# Formula Base: ER และ ERD พร้อม participation (schema V9)

เอกสารสำหรับทบทวนและนำเสนอ ณ schema migration V9; ไดอะแกรมใช้ Crow's Foot และแสดง min..max ที่ปลายแต่ละด้านตามข้อจำกัดฐานข้อมูล มิใช่การรับประกันขั้นตอนทางธุรกิจทุกสถานะ

## ไฟล์

- `formulabase-er-conceptual.mmd`: ER แบบแนวคิด แสดง entity, relationship และ participation โดยไม่แสดง attributes
- `formulabase-erd-physical.mmd`: ERD แบบตารางจริง แสดงคอลัมน์ PK, FK, UK และ participation
- `formulabase-dbdiagram-v9.sql`: SQL สำหรับนำเข้า dbdiagram เพื่อสร้างภาพเอกสารเท่านั้น **ห้ามรันกับฐานข้อมูลที่ใช้งานอยู่**

## การอ่าน Crow's Foot

- `||` = 1..1 บังคับหนึ่งรายการ (mandatory, total participation ของแถวฝั่งตรงข้าม)
- `o|` = 0..1 ไม่บังคับ อย่างมากหนึ่งรายการ (optional, partial participation)
- `o{` = 0..N ไม่บังคับและมีได้หลายรายการ
- `|{` = 1..N บังคับอย่างน้อยหนึ่งรายการ

อ่านสัญลักษณ์ที่อยู่ติด entity นั้นเป็นจำนวน entity นั้นต่อหนึ่งแถวของ entity ฝั่งตรงข้าม

ตัวอย่าง `formulas ||--o{ production_steps`: หนึ่งขั้นตอนต้องมีสูตรหนึ่งรายการ (1..1, ขั้นตอนมี total participation) แต่สูตรหนึ่งอาจมี 0..N ขั้นตอน (partial participation ของสูตร) ถึงแม้กติกาธุรกิจอาจกำหนดเงื่อนไขเพิ่มเติมก่อนนำสูตรไปใช้

ตัวอย่าง `production_plans ||--o| production_histories`: ประวัติหนึ่งรายการต้องอ้างงานหนึ่งงาน; งานหนึ่งอาจยังไม่มีประวัติหรือมีได้อย่างมากหนึ่งรายการ เพราะ `production_plan_id` เป็น NOT NULL และ UNIQUE

ตัวอย่าง `users o|--o{ production_plans`: งานหนึ่งอาจยังไม่มอบหมายหรือมีผู้รับผิดชอบหนึ่งบัญชี; ผู้ใช้หนึ่งบัญชีอาจไม่มีงานหรือรับผิดชอบหลายงาน ในฐานข้อมูลไม่ได้บังคับ role WORKER ที่ FK แต่ Backend ตรวจ role และสถานะ

## ข้อควรอธิบาย

- แยก `created_by_id`, `submitted_by_id`, `approved_by_id`, `worker_id`, `acknowledged_by_id` เป็นคนละความสัมพันธ์ แม้อ้าง `users` เหมือนกัน
- `formula_materials` คือ associative entity; คู่ `(formula_id, material_id)` ต้องไม่ซ้ำ และแต่ละแถวต้องมีสูตรและวัตถุดิบ
- `(formula_id, step_number)` และ `(production_plan_id, step_id)` ไม่ซ้ำตาม unique constraint
- `previous_revision_id` อาจเป็น NULL; โครงสร้างไม่ได้บังคับว่ารุ่นก่อนหนึ่งรุ่นมีรุ่นถัดไปได้เพียงหนึ่งรายการ จึงใช้ 0..N
- `quality_reviewed_by` เป็นข้อความ ไม่ใช่ FK ไป `users`; `audit_logs.subject_id` เป็นรหัสอ้างอิงเชิงตรรกะตาม `subject_type` ไม่ใช่ FK จึงไม่วาดเส้น FK ไปตารางอื่น
- ชนิด `timestamp` ใน Mermaid เป็นชื่อย่อเพื่อแสดงผล; SQL สำหรับ dbdiagram เก็บ `TIMESTAMP WITH TIME ZONE` ตาม schema และระบุขนาด/precision
- แผนภาพแสดงทุกคอลัมน์ของตารางแอป 11 ตาราง รวม 107 คอลัมน์ ตรวจเทียบกับ PostgreSQL 17.11 ฐานทดสอบที่รัน migration ผ่าน V9; ไม่รวม `flyway_schema_history` ซึ่งเป็นตารางติดตาม migration ของเครื่องมือ
- การนำไปตรวจ/ส่งอาจารย์: เปิดไฟล์ `.mmd` ด้วย Mermaid Live Editor หรือโปรแกรมที่รองรับ Mermaid; หากใช้ dbdiagram ให้ Import เป็น PostgreSQL ไม่ใช่ Mermaid
- นี่คือเอกสารออกแบบ ไม่ใช่คำสั่งแก้ schema ระบบจริง
