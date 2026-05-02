# ESM – Full API Endpoints Reference

## Base URLs
- Course Service: `http://localhost:8083`
- Enrollment Service: `http://localhost:8084`
- API Gateway (future): `http://localhost:8080`

## Course Microservice (`/api/v1` – future versioning)

### Courses
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /courses | List all courses |
| GET | /courses/{id} | Get course by ID |
| POST | /courses | Create course |
| PUT | /courses/{id} | Update course |
| DELETE | /courses/{id} | Delete course |

### Categories (new)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /categories | List categories |
| GET | /categories/{id} | Get category |
| POST | /categories | Create category |
| PUT | /categories/{id} | Update category |
| DELETE | /categories/{id} | Delete category |

### Modules (new)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /courses/{courseId}/modules | List modules |
| POST | /courses/{courseId}/modules | Create module |
| PUT | /modules/{id} | Update module |
| DELETE | /modules/{id} | Delete module |

### Lessons (new)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /modules/{moduleId}/lessons | List lessons |
| POST | /modules/{moduleId}/lessons | Create lesson |
| PUT | /lessons/{id} | Update lesson |
| DELETE | /lessons/{id} | Delete lesson |

### Reviews (new)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /courses/{courseId}/reviews | List reviews (paginated) |
| POST | /courses/{courseId}/reviews | Add review |

## Enrollment Microservice

### Enrollments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /enrollments | List all enrollments |
| GET | /enrollments/{id} | Get enrollment |
| GET | /enrollments/user/{userId} | List user enrollments |
| POST | /enrollments | Create enrollment |
| PUT | /enrollments/{id} | Update enrollment |
| DELETE | /enrollments/{id} | Delete enrollment |

### Progress (new)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /enrollments/{id}/progress | Get progress % |
| POST | /enrollments/{id}/lessons/{lessonId}/complete | Mark lesson complete |

### Certificates (new)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /certificates/user/{userId} | List user certificates |
| GET | /enrollments/{id}/certificate | Get certificate |
