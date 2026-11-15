# CO4 Java Assignment 4 – Event-Driven GUI Design

**Student Name:** Sudhakar R  
**Register Number:** 192311024  
**Java Version:** JDK 17 (or JDK 8+)  
**Repository name:** `CSA09-192311024-Assignment4`

## Project contents
- `Assignment_Report.md` – complete diagnoses, event tables, component hierarchies, pseudocode, traces, and complexity analyses for Tasks 1–6.
- `Task01/FeeWindow.java` & `Task01/FeeService.java` – repaired fee-payment GUI separated from JDBC/validation service.
- `Task02/README.md` – Hospital Outpatient Appointment Desk design (in `Assignment_Report.md`).
- `Task03/LibrarySearchFrame.java` – Campus Library Search Screen using nested `BorderLayout` and `FlowLayout`.
- `Task04/DrawPad.java` – completed `DrawPad` fixing `mouseClicked` and persistent `paint()`.
- `Task05/Converter.java` & `Task05/ConverterFourStyles.java` – completed lambda Currency Converter + 4 event-handling styles comparison.
- `Task06/EventRegistrationApp.java` & `Task06/schema.sql` – layered Swing–JDBC Campus Event Registration app with `SwingWorker` pagination.

## How to compile and run

```bash
cd Task01 && javac FeeService.java FeeWindow.java && java FeeWindow
cd ../Task03 && javac LibrarySearchFrame.java && java LibrarySearchFrame
cd ../Task04 && javac DrawPad.java && java DrawPad
cd ../Task05 && javac Converter.java ConverterFourStyles.java && java Converter
cd ../Task06 && javac EventRegistrationApp.java && java -cp .:mysql-connector-j.jar EventRegistrationApp
```
