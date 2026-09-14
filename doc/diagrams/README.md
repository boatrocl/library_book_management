# Diagrams — LibraFlow

ไฟล์ทั้งหมดในโฟลเดอร์นี้เขียนด้วย **PlantUML** (`.puml`)
ทุกไฟล์ผ่านการตรวจ syntax ด้วย `plantuml.jar` v1.2025.2 แล้ว (render เป็น SVG ได้สำเร็จ)

---

## รายการ Diagram

| ไฟล์ | ชนิด | ตรงกับข้อกำหนดในใบงาน |
|---|---|---|
| `01-use-case.puml` | Use Case Diagram | ✅ Use Case Diagram |
| `02-use-case-description.md` | Use Case Description | ✅ Use Case Description |
| `03-domain-model.puml` | Domain Model / Conceptual Class Diagram | ✅ Domain Model |
| `04-class-diagram.puml` | Class Diagram (แสดงตำแหน่ง Design Pattern) | ✅ Class Diagram + ตำแหน่ง Pattern |
| `05-sequence-borrow.puml` | Sequence — บันทึกการยืม | ✅ Sequence #1 |
| `06-sequence-return.puml` | Sequence — คืน + คิดค่าปรับ + แจ้งคิวจอง | ✅ Sequence #2 |
| `07-sequence-search.puml` | Sequence — ค้นหาหนังสือ (Pagination) | ✅ Sequence #3 |
| `08-activity-borrow.puml` | Activity Diagram | ✅ Activity Diagram |
| `09-state-loan.puml` | State Diagram — Loan | ✅ State Diagram |
| `10-state-bookcopy.puml` | State Diagram — BookCopy | ✅ State Diagram (เพิ่มเติม) |
| `11-er-diagram.puml` | ER Diagram / Database Schema | ✅ ER Diagram |
| `12-component-diagram.puml` | Component Diagram | ✅ Component Diagram |
| `13-deployment-diagram.puml` | Deployment Diagram | ✅ Deployment Diagram |

ครบทุกชนิดตามข้อ 9.1 ของใบงาน

---

## วิธี Render เป็นรูปภาพ

### วิธีที่ 1 — VS Code (สะดวกที่สุดตอนแก้ไข)

1. ติดตั้ง extension **PlantUML** (jebbs.plantuml)
2. ติดตั้ง Java และ Graphviz
   - Windows: `winget install Graphviz.Graphviz`
   - macOS: `brew install graphviz`
   - Ubuntu: `sudo apt install graphviz default-jre`
3. เปิดไฟล์ `.puml` แล้วกด `Alt + D` เพื่อดูตัวอย่าง
4. Export: `Ctrl + Shift + P` → `PlantUML: Export Current Diagram` → เลือก PNG หรือ SVG

### วิธีที่ 2 — Command Line (ใช้ export ทั้งโฟลเดอร์ทีเดียว)

```bash
# ดาวน์โหลด plantuml.jar จาก https://github.com/plantuml/plantuml/releases
java -jar plantuml.jar -tpng -o ./png *.puml
java -jar plantuml.jar -tsvg -o ./svg *.puml
```

### วิธีที่ 3 — เว็บ (ไม่ต้องติดตั้งอะไร)

วางโค้ดที่ https://www.plantuml.com/plantuml/uml/

---

## ⚠️ การแสดงผลภาษาไทย

PlantUML ใช้ฟอนต์ของระบบในการวาดข้อความ ถ้า render แล้วภาษาไทยกลายเป็นกล่องสี่เหลี่ยม
(□□□) แปลว่าเครื่องที่ render ไม่มีฟอนต์ที่รองรับภาษาไทย

**วิธีแก้**

1. ทุกไฟล์ตั้งค่าไว้แล้วเป็น `skinparam defaultFontName "Tahoma"` ซึ่งมีอยู่ใน Windows ทุกเครื่อง
2. บน macOS เปลี่ยนเป็น

   ```
   skinparam defaultFontName "Thonburi"
   ```

3. บน Linux / Docker ติดตั้งฟอนต์ไทยก่อน แล้วเปลี่ยนเป็น `"Noto Sans Thai"`

   ```bash
   sudo apt install fonts-thai-tlwg fonts-noto-cjk
   ```

4. หากยังมีปัญหาและต้องส่งงานด่วน ให้ export เป็น **SVG** แทน PNG
   เพราะ SVG ฝังข้อความเป็น text ทำให้เบราว์เซอร์ใช้ฟอนต์ของตัวเองแสดงผลแทน

---

## ข้อควรรู้ก่อนแก้ไข

- Diagram ทุกไฟล์อ้างอิงรหัสกฎ `BR-xx` จาก [`../project-overview.md`](../project-overview.md) — ถ้าแก้กฎ ต้องแก้ทั้งสองที่ให้ตรงกัน
- `04-class-diagram.puml` ต้องตรงกับตารางใน [`../design-patterns.md`](../design-patterns.md)
- `11-er-diagram.puml` ต้องตรงกับ [`../data-dictionary.md`](../data-dictionary.md) และ Flyway migration script
- Class Diagram ต้องสะท้อนโค้ดจริง ไม่ใช่โครงที่ตั้งใจไว้ตอนแรก — อัปเดตก่อนส่งเสมอ
