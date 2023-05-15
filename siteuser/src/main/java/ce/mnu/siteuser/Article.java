package ce.mnu.siteuser;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.persistence.*;

@Entity
public class Article {
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Long num;
	
	@Column(length=20, nullable=false)
	private String author;
	
	@Column(nullable=false, length=50)
	private String title;

	@Column(nullable=false, length=100)
	private String articledate= new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
	
	@Column(nullable=false, length=2048)
	private String body;
	
	public Long getNum( ) { return num; }
	public void setNum(Long n) { num = n; }
	
	public String getAuthor( ) { return author; }
	public void setAuthor(String e) { author = e; }
	
	public String getTitle( ) { return title; }
	public void setTitle(String e) { title = e; }
	
	public String getArticleDate() {
		return articledate;
	}
	public void setArticleDate(){
		//articledate = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
	}
	
	public String getBody( ) { return body; }
	public void setBody(String n) { body = n; }
}