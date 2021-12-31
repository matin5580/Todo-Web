package web.todo.ToDoWeb.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

@Document(collection = User.TABLE_NAME)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    public static final String TABLE_NAME = "user_table";
    public static final String FIRST_NAME = "user_table";
    public static final String LAST_NAME = "user_table";
    public static final String USER_NAME = "user_table";
    public static final String EMAIL = "user_table";
    public static final String PASSWORD = "user_table";
    public static final String PHONE_NUMBER = "user_table";
    public static final String BIRTHDAY = "user_table";
    public static final String IS_DELETED = "user_table";

    @Id
    private String id;

    private String firstName;

    private String lastName;

    @Indexed(unique = true)
    private String userName;

    @Indexed(unique = true)
    private String email;

    private String password;

    @Indexed(unique = true)
    private Long phoneNumber;

    private String birthDay;

    @Transient
    private transient int age;

    private Boolean isDeleted = Boolean.FALSE;

    private Set<ToDoFolder> toDoFolders = new TreeSet<>();

    public int getAge() {
        LocalDate birthday = LocalDate.parse(birthDay);
        LocalDate now = LocalDate.now();
        return birthday.until(now).getYears();
    }
}
