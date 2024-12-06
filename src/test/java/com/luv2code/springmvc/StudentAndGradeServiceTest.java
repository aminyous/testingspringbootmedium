package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
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

    @BeforeEach
    public void setupDatabase(){
        jdbc.execute("insert into student (id, firstname, lastname, email_address) " +
                "values (1, 'Amine', 'Hichri', 'amyn@gmail.com')");
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
        assertTrue(student.isPresent(), "return true");
        studentService.deleteStudent(1);
        student = studentDao.findById(1);
        assertFalse(student.isPresent(), "return false");

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

    @AfterEach
    public void setupAfterTransaction(){
        jdbc.execute("delete from student");
    }
}
