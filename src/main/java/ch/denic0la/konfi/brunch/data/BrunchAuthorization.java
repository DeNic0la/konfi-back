package ch.denic0la.konfi.brunch.data;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = BrunchAuthorization.TABLE_NAME)
public class BrunchAuthorization {
  public static final String TABLE_NAME = "brunch_authorization";
  public static final String COLUMN_BRUNCH_ID_NAME = "brunch_id";
  public static final String COLUMN_ADMINPASSWORDHASH_NAME = "admin_password_hash";
  public static final String COLUMN_VOTINGPASSWORDHASH_NAME = "voting_password_hash";

  @Id
  @Column(name = COLUMN_BRUNCH_ID_NAME, nullable = false, length = 50)
  private String brunch_id;

  @ToString.Exclude
  @OneToOne(optional = false, orphanRemoval = true)
  @MapsId
  @JoinColumn(name = "brunch_id", nullable = false)
  private Brunch brunch;

  @Column(name = COLUMN_ADMINPASSWORDHASH_NAME, length = 60)
  private String adminPasswordHash;

  @Column(name = COLUMN_VOTINGPASSWORDHASH_NAME, length = 60)
  private String votingPasswordHash;
}
