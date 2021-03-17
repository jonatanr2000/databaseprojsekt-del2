public class Course {

    private String courseCode;
    private String courseName;
    private String term;
    private boolean allowAnonymous;

    public Course(String courseCode, String courseName, String term, boolean allowAnonymous) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.term = term;
        this.allowAnonymous = allowAnonymous;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }
}
