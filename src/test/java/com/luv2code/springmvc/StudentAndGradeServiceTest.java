package com.luv2code.springmvc;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application.properties")
@SpringBootTest
public class StudentAndGradeServiceTest {
    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    StudentDao studentDao;
    @Autowired
    StudentAndGradeService studentService;

    @Autowired
    MathGradesDao mathGradeDao;
    @Autowired
    ScienceGradesDao scienceGradeDao;

    @Autowired
    HistoryGradesDao historyGradeDao;

    @BeforeEach
    public void setupDatabase(){
        jdbc.execute("insert into student (id, firstname, lastname, email_address) " +
                "values (1, 'Amine', 'Hichri', 'amyn@gmail.com')");

        jdbc.execute("insert into math_grade(id, student_id, grade) values (1,1,100.00)");
        jdbc.execute("insert into science_grade(id, student_id, grade) values (1,1,100.00)");
        jdbc.execute("insert into history_grade(id, student_id, grade) values (1,1,100.00)");
    }

    @Test
    public void createStudentService(){
        studentService.createStudent("Chad", "Darby", "chad@gmail.com");
        CollegeStudent student = studentDao.findByEmailAddress("chad@gmail.com");
        assertEquals("chad@gmail.com", student.getEmailAddress(), "Should be the same");
    }



    @Test
    public void isStudentNullCheck(){
        assertTrue(studentService.checkIfStudentIsNull(1));
        assertFalse(studentService.checkIfStudentIsNull(0));
    }

    @Test
    public void deleteStudentService(){
        Optional<CollegeStudent> student = studentDao.findById(1);
        Optional<MathGrade> deletedMathGrade = mathGradeDao.findById(1);
        Optional<HistoryGrade> deletedHistoryGrade = historyGradeDao.findById(1);
        Optional<ScienceGrade> deletedScienceGrade = scienceGradeDao.findById(1);



        assertTrue(student.isPresent(), "return true");
        assertTrue(deletedMathGrade.isPresent(), "return true");
        assertTrue(deletedScienceGrade.isPresent(), "return true");
        assertTrue(deletedHistoryGrade.isPresent(), "return true");

        studentService.deleteStudent(1);
        //studentService.deleteGrade(1, "math");

        student = studentDao.findById(1);
        deletedMathGrade = mathGradeDao.findById(1);
        deletedScienceGrade = scienceGradeDao.findById(1);
        deletedHistoryGrade = historyGradeDao.findById(1);

        assertFalse(student.isPresent(), "return false");
        assertFalse(deletedMathGrade.isPresent(), "return false");
        assertFalse(deletedHistoryGrade.isPresent(), "return false");
        assertFalse(deletedScienceGrade.isPresent(), "return false");

    }

    @Sql("/insertData.sql")
    @Test
    public void getGradebookService(){
        Iterable<CollegeStudent> iterableCollegeStudents = studentService.getGradebook();

        List<CollegeStudent> collegeStudents = new ArrayList<>();

        for(CollegeStudent collegeStudent: iterableCollegeStudents){
            collegeStudents.add(collegeStudent);
        }

        assertEquals(5, collegeStudents.size());

    }

    @Test
    public void createGradeService(){
        assertTrue(studentService.createGrade(80.50,1,"math"));
        assertTrue(studentService.createGrade(80.50,1,"science"));
        assertTrue(studentService.createGrade(80.50,1,"history"));

        Iterable<MathGrade> mathGrades = mathGradeDao.findGradeByStudentId(1);
        Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(1);
        Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(1);

        assertTrue(((Collection<MathGrade>) mathGrades).size() == 2, "Student has math grades");
        assertTrue(((Collection<ScienceGrade>) scienceGrades).size() == 2, "Student has science grades");
        assertTrue(((Collection<HistoryGrade>) historyGrades).size() == 2, "Student has history grades");
    }


    @Test
    public void createGradeServiceReturnFalse(){
        assertFalse(studentService.createGrade(105.00,1,"math"));
        assertFalse(studentService.createGrade(-1,1,"math"));
        assertFalse(studentService.createGrade(80.50,2,"math"));
        assertFalse(studentService.createGrade(80.50,1,"Sport"));
    }

    @Test
    public void deleteGradeService(){
        assertEquals(1, studentService.deleteGrade(1, "math"), "Returns student id after delete");
        assertEquals(1, studentService.deleteGrade(1, "science"), "Returns student id after delete");
        assertEquals(1, studentService.deleteGrade(1, "history"), "Returns student id after delete");

    }



    @Test
    public void deleteGradeServiceReturnStudentIdOfZero(){
        assertEquals(0, studentService.deleteGrade(0, "math"), "No student should have 0 id");
        assertEquals(0, studentService.deleteGrade(1, "Sport"), "No student should have a sport class");


    }

    @Test
    public void studentInformation(){
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(1);
        assertNotNull(gradebookCollegeStudent);
        assertEquals(1, gradebookCollegeStudent.getId());
        assertEquals("Amine", gradebookCollegeStudent.getFirstname());
        assertEquals("Hichri", gradebookCollegeStudent.getLastname());
        assertEquals("amyn@gmail.com", gradebookCollegeStudent.getEmailAddress());
        assertTrue(gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size() == 1);
        assertTrue(gradebookCollegeStudent.getStudentGrades().getHistoryGradeResults().size() == 1);
        assertTrue(gradebookCollegeStudent.getStudentGrades().getScienceGradeResults().size() == 1);
    }

    @Test
    public void studentInformationServiceReturnNull(){
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(0);
        assertNull(gradebookCollegeStudent);
    }

    @AfterEach
    public void setupAfterTransaction(){

        jdbc.execute("delete from student");
        jdbc.execute("delete from math_grade");
        jdbc.execute("delete from science_grade");
        jdbc.execute("delete from history_grade");
    }
}
