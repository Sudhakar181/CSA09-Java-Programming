import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class BrokenStudent {
    String regNo;
    String name;
    BrokenStudent(String regNo, String name) { this.regNo = regNo; this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrokenStudent)) return false;
        return regNo.equals(((BrokenStudent) o).regNo);
    }
    // hashCode() intentionally NOT overridden
}

class MutableFixedStudent {
    String regNo;
    String name;
    MutableFixedStudent(String regNo, String name) { this.regNo = regNo; this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MutableFixedStudent)) return false;
        return regNo.equals(((MutableFixedStudent) o).regNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regNo);
    }
}

public class AttendanceInvestigation {
    public static void main(String[] args) {
        // Test 1: Same object reference scanned twice (appears to work even without hashCode)
        Set<BrokenStudent> test1 = new HashSet<>();
        BrokenStudent arun = new BrokenStudent("192311001", "Arun");
        test1.add(arun);
        test1.add(new BrokenStudent("192311002", "Divya"));
        test1.add(arun);
        System.out.println("Test 1 (same ref, broken class) size: " + test1.size() + " (expected 2)");

        // Test 2: Different Student instances with same regNo (exposes missing hashCode)
        Set<BrokenStudent> test2 = new HashSet<>();
        test2.add(new BrokenStudent("192311001", "Arun"));
        test2.add(new BrokenStudent("192311002", "Divya"));
        test2.add(new BrokenStudent("192311001", "Arun"));
        System.out.println("Test 2 (new objs, broken class) size: " + test2.size() + " (bug: 3 instead of 2)");

        // Test 3: Mutating regNo after insertion (breaks bucket lookup)
        Set<MutableFixedStudent> test3 = new HashSet<>();
        MutableFixedStudent s = new MutableFixedStudent("192311001", "Arun");
        test3.add(s);
        s.regNo = "192311099"; // Mutated after insertion!
        System.out.println("Test 3 contains mutated student? " + test3.contains(new MutableFixedStudent("192311099", "Arun")));
        System.out.println("Test 3 contains old regNo?      " + test3.contains(new MutableFixedStudent("192311001", "Arun")));
    }
}
