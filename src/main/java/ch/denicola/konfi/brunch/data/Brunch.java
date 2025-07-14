package ch.denicola.konfi.brunch.data;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = Brunch.TABLE_NAME)
public class Brunch {
  public static final String TABLE_NAME = "brunch";
  public static final String COLUMN_ID_NAME = "id";
  public static final String COLUMN_TITLE_NAME = "title";
  public static final String COLUMN_REQUIREEMAIL_NAME = "require_email";
  public static final String COLUMN_EMAILREGEXP_NAME = "email_regexp";
  public static final String COLUMN_ADMINPASSWORD_NAME = "admin_password";
  public static final String COLUMN_VOTINGPASSWORD_NAME = "voting_password";

  @Id
  @Column(name = COLUMN_ID_NAME, nullable = false, length = 50)
  private String id;

  @Column(name = COLUMN_TITLE_NAME, nullable = false)
  private String title;

  @Column(name = COLUMN_REQUIREEMAIL_NAME, columnDefinition = "boolean default false")
  private Boolean requireEmail;

  @Column(name = COLUMN_EMAILREGEXP_NAME)
  private String emailRegexp;

  @Column(name = COLUMN_ADMINPASSWORD_NAME)
  private String adminPassword;

  @Column(name = COLUMN_VOTINGPASSWORD_NAME)
  private String votingPassword;

  @ToString.Exclude
  @OneToMany(
      mappedBy = "brunch",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  @OrderBy("order ASC")
  private List<Question> questions = new ArrayList<>();
}
