package hibernate.doc.model;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.StoredProcedureParameter;

//定义命名的rawQuery,并且映射到另一个实体上
@NamedNativeQuery(name = "student", query = "select * from student where student.age>:age", resultSetMapping = "studentDTO")
@SqlResultSetMapping(name = "studentDTO", classes = @ConstructorResult(targetClass = StudentDTO.class, columns = {
		@ColumnResult(name = "id"), @ColumnResult(name = "age"), @ColumnResult(name = "address"),
		@ColumnResult(name = "name") }))

@NamedStoredProcedureQuery(name = "stu_pro", procedureName = "sp_student", parameters = {
		@StoredProcedureParameter(mode = ParameterMode.IN, name = "inage", type = Integer.class),
		@StoredProcedureParameter(mode = ParameterMode.OUT, name = "total", type = Integer.class), })
@Entity
public class Student {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;
	private int age;
	private String address;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Student [id=" + id + ", name=" + name + ", age=" + age + ", address=" + address + "]";
	}

}
