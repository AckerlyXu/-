package hibernate.doc.model;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(OrderItem.class)
public class OrderItem_ {
	public static volatile SingularAttribute<OrderItem, Integer> id;
	public static volatile SingularAttribute<OrderItem, String> product;
	public static volatile SingularAttribute<OrderItem, Integer> count;
	public static volatile SingularAttribute<OrderItem, MyOrder> order;
}
