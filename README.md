<div align="center">

# IT Training Management

### Sistem interaktiv desktop per menaxhimin e nje qendre trajnimi te IT-se

*Nga regjistrimi i kandidatit te trajnimi certifikimi pagesa — gjithcka ne nje vend.*

<br>

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-blue?logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-build-C71A36?logo=apachemaven&logoColor=white)
![i18n](https://img.shields.io/badge/Gjuhet-SQ%20%7C%20EN%20%7C%20DE-success)
![License](https://img.shields.io/badge/License-MIT-green)

</div>

---

## Permbajtja

- [Cka eshte ky projekt](#cka-eshte-ky-projekt)
- [Funksionalitetet kryesore](#funksionalitetet-kryesore)
- [Skema e databazes](#skema-e-databazes)
- [Si te startohet (Quick Start)](#si-te-startohet-quick-start)
- [Konfigurimi](#konfigurimi)
- [Rolet e perdoruesve](#rolet-e-perdoruesve)
- [Llogarite demo](#llogarite-demo)
- [Shortcut-et](#shortcut-et)
- [Pamjet (Views)](#pamjet-views)
- [Struktura e projektit](#struktura-e-projektit)
- [Migrime & backup](#migrime--backup)
- [Troubleshooting](#troubleshooting)
- [Licenca](#licenca)

---

## Cka eshte ky projekt

Aplikacion desktop **JavaFX + MySQL** i ndertuar per lenden **Komunikimi Njeri–Kompjuter (KNK)**.
Mbulon te gjithe ciklin jeteanesor te nje kandidati ne nje qender trajnimi:

```
Regjistrim    Trajnim    Ndjekje statusi    Pagesa    Certifikate
```

I ndertuar me ndarje te paster ne shtresa (**Controller  Service  Repository  Model/DTO/Mapper**), me kontroll qasjeje sipas roleve, audit log ne databaze, reset password me email real dhe nderfaqe trigjuhesh.

---

## Funksionalitetet kryesore

| Funksioni | Pershkrimi |
|---|---|
| **Autentikim & role** | Login me hash te password-it, sesion aktiv, qasje e kontrolluar per `ADMIN` / `STAFF` / `TRAINER` |
| **Menaxhim kandidatesh** | Krijim, editim, fshirje dhe ndjekje statusi ne `TableView` me menu, toolbar, status bar |
| **Trajnime** | Nivel, pershkrim, data fillimi/mbarimi, kapacitet dhe lidhje me trajnerin |
| **Pagesa** | Regjistrim & menaxhim pagesash te lidhura me kandidatin (vetem `ADMIN`) |
| **Certifikata** | Leshim certifikatash per kandidatet e perfunduar |
| **Dashboard BI/AI** | Statistika me `PieChart`, `BarChart` dhe rekomandim dinamik |
| **Trigjuhesh** | Shqip · Anglisht · Gjermanisht — me flamuj dhe ndihme te integruar |
| **Reset password** | Token reale ne databaze + dergim email me Jakarta Mail (SMTP) |
| **Accessibility** | Shortcut-e, renditje Tab, tooltip dhe accessible text |
| **Audit log** | Trigger-a ne databaze qe regjistrojne ndryshimet ne kandidate & trajnime |

---

## Skema e databazes

Skema `it_training_center_db` perfshin role/permissions, perdorues, trajnime, kandidate, pagesa, certifikata, tokena reset-i dhe nje audit log te mbushur nga trigger-at.

Lidhjet kryesore (1 -> shume):

```text
roles ───< users                 (users.role_id)
roles ───< role_permissions       (role_permissions.role_id)
permissions ───< role_permissions (role_permissions.permission_id)
users ───< trainings              (trainings.trainer_user_id)
users ───< password_reset_tokens  (password_reset_tokens.user_id)
trainings ───< candidates         (candidates.training_id)
candidates ───< payments          (payments.candidate_id)
candidates ───< certificates      (certificates.candidate_id)
audit_log  ◄── mbushet nga trigger-at e candidates & trainings
```

| Tabela | Fushat kryesore |
|---|---|
| `roles` | id, name |
| `permissions` | id, name |
| `role_permissions` | role_id, permission_id |
| `users` | id, username, email, role_id |
| `trainings` | id, title, trainer_user_id, trainer_email |
| `candidates` | id, name, training_id, status |
| `payments` | id, candidate_id, amount |
| `certificates` | id, candidate_id, issued_at |
| `password_reset_tokens` | id, user_id, token |
| `audit_log` | id, entity, action, ts |

---

## Si te startohet (Quick Start)

**Parakushtet:** Java **21+**, **Maven** dhe **MySQL** lokal.

**1. Krijo databazen** (rikrijon skemen + seed data per demonstrim):

```sql
-- ekzekuto ne MySQL:
SOURCE database/databaza_total.sql;
```

**2. Startoje aplikacionin:**

```powershell
mvn clean javafx:run
```

**3. Kycu** me llogarine demo `admin / admin123` — [shih me poshte](#llogarite-demo).

---

## Konfigurimi

Konfigurimi default i lidhjes me databazen:

```text
host:     localhost
port:     3306
database: it_training_center_db
user:     root
password: (bosh)
```

<details>
<summary><b> Nese MySQL ka password tjeter / port tjeter (kliko)</b></summary>

<br>

Vendos **VM options** ne IntelliJ:

```text
-Ddb.user=root -Ddb.password=PASSWORDI_JUAJ
```

Ose ndrysho komplet URL-ne:

```text
-Ddb.url=jdbc:mysql://localhost:3306/it_training_center_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

</details>

<details>
<summary><b> Reset password me email real — konfigurimi SMTP (kliko)</b></summary>

<br>

Shembull me **Gmail App Password** (VM options):

```text
-Dsmtp.host=smtp.gmail.com -Dsmtp.port=587 -Dsmtp.user=emaili@gmail.com -Dsmtp.password=APP_PASSWORD -Dsmtp.from=emaili@gmail.com
```

Te njejtat mund te vendosen edhe si **environment variables**:

```text
SMTP_HOST · SMTP_PORT · SMTP_USER · SMTP_PASSWORD · SMTP_FROM
```

>  Pa konfigurim SMTP, tokeni i reset-it gjenerohet por nuk dergohet email real.

</details>

---

## Rolet e perdoruesve

| Roli | Si krijohet | Cka mund te beje |
|---|---|---|
| **ADMIN** | ekziston ne seed | Qasje e plote: fshin kandidate, menaxhon pagesa, gjeneron certifikata, krijon staf & trajnere |
| **STAFF** | nga faqja `Stafi` (vetem ADMIN e sheh) | Regjistron kandidate & trajnime. **Nuk** krijon trajnere, **nuk** sheh pagesa, **nuk** leshon certifikata |
| **TRAINER** | nga faqja `Trajnimet` *Krijo llogari per trajnerin* | Sheh vetem kandidatet e trajnimeve te veta, ndryshon statusin dhe leshon certifikata per kandidatet e vet |

**Konvencioni i llogarive te krijuara nga admini** (staf ose trajner):

```text
Emri Mbiemri    username: emri_mbiemri   password: emri123
```

> Shembull: `Arta Krasniqi`  `arta_krasniqi` / `arta123`

---

## Llogarite demo

```text
 ADMIN
username: admin
password: admin123
```

---

## Shortcut-et

| Tasti | Veprimi |
|---|---|
| `Ctrl + N` | Kandidat i ri |
| `Ctrl + S` | Ruajtje |
| `Delete` | Fshirje e kandidatit te zgjedhur |
| `F5` | Rifreskim |
| `F1` | Ndihme |

---

## Pamjet (Views)

| Skedari FXML | Pershkrimi |
|---|---|
| `login-view.fxml` | Hyrja ne sistem |
| `reset-password-view.fxml` | Kerkese per reset password |
| `main-view.fxml` | Dashboard kryesor + kandidatet |
| `trainings-view.fxml` | Trajnimet |
| `payments-view.fxml` | Pagesat |
| `certificates-view.fxml` | Certifikatat |
| `staff-view.fxml` | Menaxhimi i stafit (vetem ADMIN) |
| `help-view.fxml` | Ndihma e integruar |

---

## Struktura e projektit

```text
src/
  main/
    java/
      app/            pika hyrese e aplikacionit (App.java)
      controllers/    JavaFX controllers
      exceptions/     gabime te personalizuara
      models/         entitete / domain models
        dto/          objekte per transferim te te dhenave
        mappers/      konvertim mes databazes dhe modeleve
      repository/     qasja ne te dhena
      services/       logjika e biznesit (Auth, Email, I18n, ...)
      module-info.java
    resources/
      views/          FXML views per Scene Builder
      i18n/           perkthimet sq / en / de
      styles/         app.css
      images/flags/   flamujt al / gb / de
  test/
    java/             teste JUnit
database/             skripta SQL + migrime
docs/                 dokumentim, kerkesat dhe ndarja e punes
```

---

## Migrime & backup

**Databaze e re nga zero** — ekzekuto:

```text
database/databaza_total.sql
```

**Databaze ekzistuese** (perditeso pa e rikrijuar) — ekzekuto sipas radhes:

```text
database/002_trainers_permissions_upgrade.sql
database/003_training_trainer_email_upgrade.sql
```

**Backup manual** me `mysqldump` (dosja `database/backups/` injorohet nga git):

```powershell
mysqldump -u root -p it_training_center_db > database/backups/backup.sql
```

---

## Troubleshooting

<details>
<summary><b>Access denied for user 'root'@'localhost'</b></summary>

MySQL ka password. Shtoje ne VM options: `-Ddb.user=root -Ddb.password=PASSWORDI_JUAJ`
</details>

<details>
<summary><b>Unknown database 'it_training_center_db'</b></summary>

Nuk e ke ekzekutuar skripten. Ekzekuto `database/databaza_total.sql` ne MySQL.
</details>

<details>
<summary><b>"release version 21 not supported" ose gabim moduli</b></summary>

Kontrollo qe JDK-ja aktive eshte **21+** (`java -version`) dhe e zgjedhur si SDK ne IntelliJ.
</details>

<details>
<summary><b>Emaili i reset-it nuk dergohet</b></summary>

Mungon konfigurimi SMTP. Shih seksionin [Konfigurimi](#konfigurimi). Me Gmail duhet **App Password**, jo password-i normal.
</details>

---

## Licenca

Ky projekt eshte i licencuar nen licencen **MIT** — shih [`LICENSE`](LICENSE).

<div align="center">

---

*Ndertuar me Java & JavaFX per lenden Komunikimi Njeri–Kompjuter*

</div>