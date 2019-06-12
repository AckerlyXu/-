package hibernate.doc.model;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Student.class)
public class Student_ {

	public static volatile SingularAttribute<Student, Integer> id;
	public static volatile SingularAttribute<Student, String> name;
	public static volatile SingularAttribute<Student, Integer> age;
	public static volatile SingularAttribute<Student, String> address;
}
