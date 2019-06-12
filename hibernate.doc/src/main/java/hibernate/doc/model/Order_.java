package hibernate.doc.model;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(MyOrder.class)
public class Order_ {
	public static volatile SingularAttribute<MyOrder, Integer> id;
	public static volatile SingularAttribute<MyOrder, String> orderNumber;
	public static volatile ListAttribute<MyOrder, OrderItem> orderItems;

}
