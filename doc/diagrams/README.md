# LibraFlow diagrams

PlantUML sources are stored in this directory. The Tech Stack and System Architecture illustrations are authored as scalable SVGs in `images/`; PNG exports are in `png/`. The README embeds the PNG exports so the bundled Press Start 2P pixel headings render consistently on GitHub. The diagrams were synchronized on 10 October 2026 with the current controller, service, migration, and workflow configuration. The deployment diagram shows the post-deploy production smoke check separately from CI and the GitHub Pages teaching demo.

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
| 14 | Technology stack | [SVG source](images/14-tech-stack.svg) | [PNG](png/14-tech-stack.png) | [SVG](images/14-tech-stack.svg) |
| 15 | System architecture | [SVG source](images/15-system-architecture.svg) | [PNG](png/15-system-architecture.png) | [SVG](images/15-system-architecture.svg) |

## Render exports

With PlantUML 1.2025.2 and Java installed, run these commands from this directory:

```bash
java -jar /path/to/plantuml-1.2025.2.jar -tpng -o png *.puml
java -jar /path/to/plantuml-1.2025.2.jar -tsvg -o images *.puml
```

The Tech Stack and System Architecture SVG files are the editable sources. They use the bundled Press Start 2P typeface for pixel headings and Noto Sans Mono for supporting text. The font is licensed under the SIL Open Font License 1.1; the license file is included in the fonts directory.

From this directory, install the font and refresh the PNG previews with:

```bash
mkdir -p ~/.local/share/fonts
cp fonts/PressStart2P-Regular.ttf ~/.local/share/fonts/
fc-cache -f ~/.local/share/fonts
rsvg-convert -o png/14-tech-stack.png images/14-tech-stack.svg
rsvg-convert -o png/15-system-architecture.png images/15-system-architecture.svg
```

PlantUML uses Tahoma in the source for Thai labels. Install a Thai-capable font if its PNG output shows missing glyphs.

## Review when implementation changes

- Compare the ER diagram with all Flyway migrations through V10 and [the data dictionary](../data-dictionary.md).
- Compare class and component diagrams with the service, repository, mapper, and pattern classes.
- Keep loan, return, and copy-state diagrams aligned with `LoanServiceImpl`, `LoanScheduler`, `ReservationServiceImpl`, and [business rules](../project-overview.md).
- The current return API accepts a loan ID and records all outstanding items in that loan. It does not accept a damage condition.
- The deployment diagram separates production hosting from CI and the isolated [CD teaching demo](../cd-demo.md). A public URL alone does not prove the configured provider branch.
