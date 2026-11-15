# CO4 Java Assignment 4
## Event-Driven GUI Design (AWT & Swing)

**Student Name:** Sudhakar R  
**Register Number:** 192311024  
**GitHub Link:** `https://github.com/<your-username>/CSA09-192311024-Assignment4`  
**Java Version:** JDK 17 (or JDK 8+)

> Run every GUI program in `Task01`, `Task03`, `Task04`, `Task05`, and `Task06` on your machine and attach screenshots linked to each tested interaction.

---

## Task 1 – GUI Design Review: FeeWindow

### Task 1.1 – Diagnose the Design (7 Deficiencies)
1. **No separation of UI and logic:** Validation, SQL building, and JDBC calls sit inside `actionPerformed()`. Changing DB rules forces editing the GUI class.
2. **String comparison with `==`:** `e.getActionCommand() == "Pay"` compares object references instead of string content; if the command string is dynamically created, it fails silently and runs the `else` branch (clearing fields!).
3. **Unvalidated / empty input:** `Double.parseDouble(amount.getText())` throws an uncaught `NumberFormatException` on empty or non-numeric input, crashing the handler with no message to the user.
4. **Blocking the Event-Dispatch Thread (EDT):** `DriverManager.getConnection(...)` and `Thread.sleep(3000)` freeze the entire window for 3+ seconds so repaints, clicks, and window close stop responding.
5. **No GUI feedback:** `System.out.println("Paid")` writes only to the console; the user sees nothing in the window.
6. **SQL Injection & unclosed resources:** String concatenation in `INSERT INTO fees VALUES(...)` allows SQL injection, and `Connection`/`Statement` are never closed (resource leak). Also, the `Receipt` button has no `ActionListener` (clicking it triggers `else` and wipes the inputs!) and the `Frame` has no `WindowListener` (X button does nothing).
7. **Unhandled checked exceptions:** `SQLException` and `InterruptedException` are not caught, so the original code does not even compile.

### Task 1.2 – Repair the Design (`Task01/FeeWindow.java`, `Task01/FeeService.java`)
- `FeeService` handles input validation, `PreparedStatement` parameterization, and `try-with-resources` JDBC cleanup.
- `FeeWindow` collects input, runs `FeeService.validateAndPay()` inside a `SwingWorker` (off the EDT), and updates a `JLabel` status bar + `JOptionPane` dialog.

### Task 1.3 – Design Defence (< 100 words)
Separating `FeeService` from `FeeWindow` allows unit testing validation and payment logic directly without launching a GUI or clicking buttons. SQL changes, validation rules, or database drivers can be updated in `FeeService` without touching Swing layout code, and background execution keeps the interface responsive.

### Alternative Concept & Complexity Analysis
Giving each button its own listener or `Swing Action` dispatches events in **`O(1)`** direct method call (vs up to **`O(k)`** string comparisons in an `if–else` chain for `k` buttons). It uses **`O(k)`** small listener objects in memory instead of `O(1)` shared listener, which is negligible and far cleaner.

---

## Task 2 – Hospital Outpatient Appointment Desk

### Task 2.1 – Model the Events

| Event Source | Event Object | Listener Interface | Action Taken |
|---|---|---|---|
| Department `JComboBox` | `ItemEvent` / `ActionEvent` | `ItemListener` / `ActionListener` | Refresh doctor `JComboBox` for selected department |
| Doctor `JComboBox` | `ActionEvent` | `ActionListener` | Mark booking in progress; enable time slots |
| Time-slot `JList` / `JComboBox` | `ListSelectionEvent` / `ActionEvent` | `ListSelectionListener` | Store selected slot |
| `Book` `JButton` | `ActionEvent` | `ActionListener` | Validate patient ID & selections; book & show confirmation/error |
| Main `JFrame` (close X) | `WindowEvent` | `WindowListener` (`WindowAdapter`) | Prompt confirmation dialog if booking is in progress |

### Task 2.2 – Component Hierarchy
```text
JFrame ("Hospital Outpatient Appointment Desk", BorderLayout)
 ├── NORTH: JLabel ("Outpatient Appointment Booking")
 ├── CENTER: JPanel (GridLayout 4x2)
 │    ├── JLabel("Patient ID:")    | JTextField
 │    ├── JLabel("Department:")    | JComboBox<String>
 │    ├── JLabel("Doctor:")        | JComboBox<Doctor>
 │    └── JLabel("Time Slot:")     | JComboBox<String>
 └── SOUTH: JPanel (BorderLayout)
      ├── NORTH: JPanel (FlowLayout) -> JButton("Book"), JButton("Reset")
      └── SOUTH: JLabel (status / confirmation / error message)
```

### Task 2.3 – Handler Logic Pseudocode
```text
PROCEDURE onDepartmentChanged(selectedDept)
    doctors <- deptDoctorMap.get(selectedDept)
    doctorComboBox.removeAllItems()
    FOR EACH doc IN doctors
        doctorComboBox.addItem(doc)
    END FOR
    bookingInProgress <- TRUE
END PROCEDURE

PROCEDURE onBookClicked()
    pid <- patientIdField.getText().trim()
    IF pid IS EMPTY THEN
        showError("Patient ID is required")
        RETURN
    END IF
    doc <- doctorComboBox.getSelectedItem()
    slot <- slotComboBox.getSelectedItem()
    IF doc IS NULL OR slot IS NULL THEN
        showError("Select both doctor and time slot")
        RETURN
    END IF
    saveAppointment(pid, doc, slot)
    bookingInProgress <- FALSE
    showConfirmation("Booked " + pid + " with " + doc + " at " + slot)
END PROCEDURE
```

### Task 2.4 – Event Sequence Trace & Applet Comparison
1. `new AppointmentFrame()` builds components -> `setVisible(true)` -> `windowOpened(WindowEvent)` -> `paint(Graphics)`.
2. User edits/selects -> `itemStateChanged` (department) -> `actionPerformed` (Book) -> `windowClosing` (user clicks X; if confirmed, `dispose()`) -> `windowClosed`.
- **Applet comparison:** An applet is browser-managed via `init() -> start() -> paint() -> stop() -> destroy()`, whereas a Swing `JFrame` is application-managed via constructor, `setVisible(true)`, `WindowListener` events, and `dispose()`.

### Alternative Concept & Complexity Analysis
- **Preloaded `Map<String, List<Doctor>>`:** Department change lookup is **`O(1)`** in memory; total memory is **`O(d * m)`** for `d` departments and `m` doctors each.
- **DB query per department change:** Time per change is **`O(network + DB query + m)`** (tens of milliseconds, requires `SwingWorker`), while memory stays **`O(m)`** (only one department's doctors held at a time).

---

## Task 3 – Campus Library Search Screen

### Task 3.1 – Layout Manager Choices (`Task03/LibrarySearchFrame.java`)
- **Main `JFrame` (`BorderLayout`):** `CENTER` automatically expands to absorb all extra vertical and horizontal space when resized, while `NORTH` and `SOUTH` keep their preferred heights.
- **Top Search Row (`FlowLayout`):** Keeps `JTextField`, category `JComboBox`, and `Search` button at their natural preferred sizes in a single horizontal row.
- **Bottom Button Row (`FlowLayout`):** Keeps `Prev`, `Next`, and `Reserve` centered at fixed preferred sizes.

### Task 3.2 – Nested Container Diagram
```text
JFrame (BorderLayout)
 ├── NORTH: topContainer (BorderLayout)
 │    ├── NORTH: JLabel ("Campus Library Catalog Search")
 │    └── SOUTH: searchRow (FlowLayout) -> [JLabel, JTextField, JComboBox, JButton("Search")]
 ├── CENTER: JScrollPane -> JTextArea (grows on window resize)
 └── SOUTH: bottomContainer (BorderLayout)
      ├── NORTH: buttonRow (FlowLayout) -> [Prev, Next, Reserve]
      └── SOUTH: JLabel (statusLabel)
```

### Task 3.4 – Resizing Behaviour
When tested at small (`500x300`), medium (`750x450`), and maximized sizes, the `CENTER` `JScrollPane` grows proportionally in both width and height while the `NORTH` search bar and `SOUTH` buttons retain their heights.

### Alternative Concept & Complexity Analysis
- **`GridBagLayout`:** Achieves the same resize behavior with fewer nested `JPanel`s (`O(1)` container vs `O(4)` nested panels), but its constraint-grid solver has higher layout computation cost and verbose `GridBagConstraints` code.
- **Absolute positioning (`setLayout(null)` + `setBounds`):** `O(n)` trivial placement, `0` extra containers, **fails completely on resize** (components do not grow or reflow unless manually recalculated in a `ComponentListener`).

---

## Task 4 – Event-Handling Forensics: DrawPad

- **Task 4.1 – Defect:** The method was misspelled as `mouseclicked` (lowercase `c`) instead of `mouseClicked(MouseEvent e)`. Thus `MouseAdapter.mouseClicked` was never overridden, `count` stayed `0`, and nothing was drawn.
- **Task 4.2 – Failing Interaction:** After fixing the spelling, clicking draws dots via `getGraphics()`. Minimizing and restoring (or dragging another window over it) triggers AWT/Swing to repaint the frame, which erases all transient `getGraphics()` drawings because `paint()` does not know about them.
- **Task 4.3 – Repair (`Task04/DrawPad.java`):** Store every clicked `Point` in `List<Point> points`, update `status`, call `repaint()`, and draw all stored points inside `@Override public void paint(Graphics g)`.
- **Task 4.4 – Prevention:** Adding `@Override` above `public void mouseclicked(MouseEvent e)` causes a **compile-time error**: `method does not override or implement a method from a supertype`.

### Alternative Concept & Complexity Analysis
- **Stored `List<Point>` + `paint()`:** Time per repaint is **`O(n)`** for `n` clicks; space is **`O(n)`** `Point` objects.
- **Off-screen `BufferedImage`:** Time per repaint is **`O(width * height)`** (constant `O(1)` with respect to `n` clicks); space is **`O(width * height)`** pixels regardless of how few or many dots are drawn.

---

## Task 5 – Same Screen, Different Event-Handling Styles

- **Completed Lambda Version:** `Task05/Converter.java` (one `ActionListener` lambda attached to both `convertButton` and `amount` Enter key).
- **All 4 Styles + Dialog Output:** `Task05/ConverterFourStyles.java`.
- **Change Effort (Task 5.2):** In styles (a), (b), and (d), adding the Enter-key trigger required **1 line** (`amount.addActionListener(...)`) because the listener was already named/reusable. In style (c) (anonymous inner class), either the anonymous class must be duplicated or extracted to a local variable first.

| Style | Readability | Field Access | Reuse | Ease of Change |
|---|---|---|---|---|
| (a) Frame implements `ActionListener` | Low (giant `if-else` for many buttons) | Direct | Poor | Poor |
| (b) Named inner class | Good | Direct | Good across triggers | Good |
| (c) Anonymous inner class | Cluttered for long handlers | Direct (needs `final`/effectively final locals) | Poor (inline) | Moderate |
| (d) Lambda expression | Very high (concise) | Direct | High when assigned to `ActionListener` | Very high |

- **Large application choice:** **Lambdas / method references (`this::convert`) or Swing `Action` objects** for clean separation and reuse.

### Alternative Concept & Complexity Analysis
Using a single shared `Action` (or `this::convert`) across a button, menu item, and keyboard shortcut creates **`1` listener/Action object** (`O(1)` space) instead of `k` separate objects, and adding a new trigger takes **1 line** (`menuItem.setAction(convertAction)`).

---

## Task 6 – Design Challenge: Campus Event Registration Desktop Application

### Stage 1 – Interpret the Problem
- **Users:** Students (registering) and Cultural Committee (viewing, filtering, exporting).
- **Rules:** Reject empty/invalid register numbers (`[A-Za-z0-9]{5,20}`), prevent duplicate `(student_reg_no, event_name)` registrations, keep UI responsive during DB calls.

### Stage 2 & Stage 3 – Screen Flow & Critical Design Decisions
- Single `JFrame` (`Task06/EventRegistrationApp.java`) with `JMenuBar` (`File -> Export CSV, Exit`), top registration form, center `JTable`, bottom filter/pagination controls, and status bar.
- **Validation:** In `RegistrationService` before any DB call.
- **Duplicate check:** Enforced both in MySQL (`UNIQUE (student_reg_no, event_name)` in `Task06/schema.sql`) and caught as `SQLIntegrityConstraintViolationException` in `RegistrationService`.
- **Responsiveness:** All JDBC inserts and paginated `SELECT` queries run inside `SwingWorker.doInBackground()`, never on the EDT.

### Stage 4 – Layered Architecture
```text
EventRegistrationApp (Swing GUI / Presentation)
        │  calls on SwingWorker thread
        ▼
RegistrationService (Validation & Business Rules)
        │  calls JDBC
        ▼
RegistrationDao (PreparedStatement + LIMIT/OFFSET) ──► MySQL (campus_events.event_registrations)
```

### Alternative Concept & Complexity Analysis (100,000 Registrations)
- **Loading all 100,000 rows at once:** High query + transfer time (seconds), **`O(N)`** memory (~tens of MB for `Object[]` rows in `DefaultTableModel`), and slow `JTable` model updates.
- **Paginated `LIMIT 50 OFFSET k` in `SwingWorker`:** Constant **`O(page_size)`** memory (50 rows at a time), sub-50 ms load per page, and zero EDT freezing.
