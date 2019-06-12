package hibernate.doc.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;

@Entity
@NamedNativeQuery(name = "order", query = "select o.id, o.orderNumber , oi.count, oi.order_id,oi.id,oi.product from myorder o join orderitem oi on o.id = oi.order_id", resultSetMapping = "order_orderitem")

@SqlResultSetMapping(name = "order_orderitem", entities = { @EntityResult(entityClass = MyOrder.class, fields = {
		@FieldResult(name = "id", column = "o.id"), @FieldResult(name = "orderNumber", column = "o.orderNumber"),

		}),
		@EntityResult(entityClass = OrderItem.class, fields = { @FieldResult(name = "id", column = "oi.id"),
				@FieldResult(name = "count", column = "oi.count"), @FieldResult(name = "order", column = "oi.order_id"),
				@FieldResult(name = "product", column = "oi.product") })

})

public class MyOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String orderNumber;
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> orderItems = new ArrayList<OrderItem>();

	public List<OrderItem> getOrderItem() {
		return orderItems;
	}

	public void addOrderItem(OrderItem item) {
		orderItems.add(item);
		item.setOrder(this);

	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public Integer getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Order [id=" + id + ", orderNumber=" + orderNumber + "]";
	}

}
