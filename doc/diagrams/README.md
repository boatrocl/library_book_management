# LibraFlow diagrams

ไฟล์ `.puml` เป็น source ที่แก้ไขได้ ภาพ SVG สำหรับขยายดูรายละเอียดอยู่ใน `images/`
และ PNG สำหรับเอกสาร/การนำเสนออยู่ใน `png/`. สร้างและตรวจ render ด้วย PlantUML 1.2025.2
วันที่ 8 ต.ค. 2569.

| # | Diagram | Source | PNG | SVG |
|---|---|---|---|---|
| 1 | Use Case | [01-use-case.puml](01-use-case.puml) | [PNG](png/01-use-case.png) | [SVG](images/01-use-case.svg) |
| 2 | Use Case Description | [02-use-case-description.md](02-use-case-description.md) | — | — |
| 3 | Domain Model | [03-domain-model.puml](03-domain-model.puml) | [PNG](png/03-domain-model.png) | [SVG](images/03-domain-model.svg) |
| 4 | Class Diagram and patterns | [04-class-diagram.puml](04-class-diagram.puml) | [PNG](png/04-class-diagram.png) | [SVG](images/04-class-diagram.svg) |
| 5 | Sequence — borrow | [05-sequence-borrow.puml](05-sequence-borrow.puml) | [PNG](png/05-sequence-borrow.png) | [SVG](images/05-sequence-borrow.svg) |
| 6 | Sequence — return, fine and reservation | [06-sequence-return.puml](06-sequence-return.puml) | [PNG](png/06-sequence-return.png) | [SVG](images/06-sequence-return.svg) |
| 7 | Sequence — search and pagination | [07-sequence-search.puml](07-sequence-search.puml) | [PNG](png/07-sequence-search.png) | [SVG](images/07-sequence-search.svg) |
| 8 | Activity — borrow | [08-activity-borrow.puml](08-activity-borrow.puml) | [PNG](png/08-activity-borrow.png) | [SVG](images/08-activity-borrow.svg) |
| 9 | State — loan | [09-state-loan.puml](09-state-loan.puml) | [PNG](png/09-state-loan.png) | [SVG](images/09-state-loan.svg) |
| 10 | State — book copy | [10-state-bookcopy.puml](10-state-bookcopy.puml) | [PNG](png/10-state-bookcopy.png) | [SVG](images/10-state-bookcopy.svg) |
| 11 | ER / database schema | [11-er-diagram.puml](11-er-diagram.puml) | [PNG](png/11-er-diagram.png) | [SVG](images/11-er-diagram.svg) |
| 12 | Component | [12-component-diagram.puml](12-component-diagram.puml) | [PNG](png/12-component-diagram.png) | [SVG](images/12-component-diagram.svg) |
| 13 | Deployment topology | [13-deployment-diagram.puml](13-deployment-diagram.puml) | [PNG](png/13-deployment-diagram.png) | [SVG](images/13-deployment-diagram.svg) |

## Render again

From this directory, with Java installed and `plantuml.jar` available:

```bash
java -jar plantuml.jar -tpng -o png *.puml
java -jar plantuml.jar -tsvg -o images *.puml
```

The checked-in exports use subfolders `images/` with separate PNG and SVG files. To reproduce
that layout from the repository root in PowerShell:

```powershell
$files = Get-ChildItem doc/diagrams/*.puml | ForEach-Object { $_.FullName }
java -jar plantuml.jar -tpng -o png $files
java -jar plantuml.jar -tsvg -o images $files
```

PlantUML uses the `Tahoma` font for Thai labels. If Thai glyphs are missing on Linux, install
a Thai-capable font and adjust `skinparam defaultFontName` in the source before exporting.

## Keep diagrams aligned with code

- ER diagram: compare with every Flyway migration and [`../data-dictionary.md`](../data-dictionary.md).
- Class diagram: compare with actual classes and [`../design-patterns.md`](../design-patterns.md).
- Loan/return sequences and state diagrams: keep aligned with `LoanServiceImpl`, `LoanScheduler`
  and the current business rules in [`../project-overview.md`](../project-overview.md).
- Deployment diagram is intended topology. A public service URL does not prove the deployed
  Git branch or the database branch selected by its connection string; verify both in provider dashboards.
