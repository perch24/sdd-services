package io.perch.services.course;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/course")
@Api(value = "course", description = "Course API", tags = "course")
public class CourseController {
    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Get All Courses", nickname = "GetCourses")
    public List<Course> findAll() {
        return (List<Course>) courseRepository.findAll();
    }
}
