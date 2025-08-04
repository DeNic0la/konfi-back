package ch.denic0la.konfi.brunch.data;

import java.util.Objects;

import org.hibernate.proxy.HibernateProxy;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = VoteAnswer.TABLE_NAME)
public class VoteAnswer {
  public static final String TABLE_NAME = "vote_answer";
  public static final String COLUMN_ID_NAME = "id";
  public static final String COLUMN_KONFIDENCEVALUE_NAME = "konfidence_value";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = COLUMN_ID_NAME, nullable = false)
  private Integer id;

  @ToString.Exclude
  @ManyToOne(optional = false)
  @JoinColumn(name = "answer_to_id", nullable = false)
  private Question answerTo;

  @Column(name = COLUMN_KONFIDENCEVALUE_NAME, nullable = false)
  private Integer konfidenceValue;

  @ToString.Exclude
  @ManyToOne(cascade = CascadeType.ALL, optional = false)
  @JoinColumn(name = "vote_id", nullable = false)
  private Vote vote;

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    VoteAnswer that = (VoteAnswer) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
