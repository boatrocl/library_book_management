# LibraFlow diagrams

PlantUML source is stored in this directory. Rendered SVGs are in `images/`; PNGs are in `png/`. The source diagrams and exports were synchronized on 10 October 2026 with the current controller, service, migration, and workflow configuration. The deployment diagram shows the post-deploy production smoke check separately from CI and the GitHub Pages teaching demo.

| # | Diagram | Source | PNG | SVG |
|---|---|---|---|---|
| 1 | Use cases | [01-use-case.puml](01-use-case.puml) | [PNG](png/01-use-case.png) | [SVG](images/01-use-case.svg) |
| 2 | Use case descriptions | [02-use-case-description.md](02-use-case-description.md) | — | — |
| 3 | Domain model | [03-domain-model.puml](03-domain-model.puml) | [PNG](png/03-domain-model.png) | [SVG](images/03-domain-model.svg) |
| 4 | Class diagram and patterns | [04-class-diagram.puml](04-class-diagram.puml) | [PNG](png/04-class-diagram.png) | [SVG](images/04-class-diagram.svg) |
| 5 | Borrow sequence | [05-sequence-borrow.puml](05-sequence-borrow.puml) | [PNG](png/05-sequence-borrow.png) | [SVG](images/05-sequence-borrow.svg) |
| 6 | Return, fine, and reservation sequence | [06-sequence-return.puml](06-sequence-return.puml) | [PNG](png/06-sequence-return.png) | [SVG](images/06-sequence-return.svg) |
| 7 | Search and pagination sequence | [07-sequence-search.puml](07-sequence-search.puml) | [PNG](png/07-sequence-search.png) | [SVG](images/07-sequence-search.svg) |
| 8 | Borrow activity | [08-activity-borrow.puml](08-activity-borrow.puml) | [PNG](png/08-activity-borrow.png) | [SVG](images/08-activity-borrow.svg) |
| 9 | Loan state | [09-state-loan.puml](09-state-loan.puml) | [PNG](png/09-state-loan.png) | [SVG](images/09-state-loan.svg) |
| 10 | Book copy state | [10-state-bookcopy.puml](10-state-bookcopy.puml) | [PNG](png/10-state-bookcopy.png) | [SVG](images/10-state-bookcopy.svg) |
| 11 | ER / database schema | [11-er-diagram.puml](11-er-diagram.puml) | [PNG](png/11-er-diagram.png) | [SVG](images/11-er-diagram.svg) |
| 12 | Components | [12-component-diagram.puml](12-component-diagram.puml) | [PNG](png/12-component-diagram.png) | [SVG](images/12-component-diagram.svg) |
| 13 | Deployment and CI/CD boundaries | [13-deployment-diagram.puml](13-deployment-diagram.puml) | [PNG](png/13-deployment-diagram.png) | [SVG](images/13-deployment-diagram.svg) |

## Render exports

With PlantUML 1.2025.2 and Java installed, run these commands from this directory:

```bash
java -jar /path/to/plantuml-1.2025.2.jar -tpng -o png *.puml
java -jar /path/to/plantuml-1.2025.2.jar -tsvg -o images *.puml
```

PlantUML uses Tahoma in the source for Thai labels. On Linux, install a Thai-capable font if PNG output shows missing glyphs; the SVG output retains the font-family declaration and can be opened at any zoom level.

## Review when implementation changes

- Compare the ER diagram with all Flyway migrations through V10 and [the data dictionary](../data-dictionary.md).
- Compare class and component diagrams with the service, repository, mapper, and pattern classes.
- Keep loan, return, and copy-state diagrams aligned with `LoanServiceImpl`, `LoanScheduler`, `ReservationServiceImpl`, and [business rules](../project-overview.md).
- The current return API accepts a loan ID and records all outstanding items in that loan. It does not accept a damage condition.
- The deployment diagram separates production hosting from CI and the isolated [CD teaching demo](../cd-demo.md). A public URL alone does not prove the configured provider branch.
