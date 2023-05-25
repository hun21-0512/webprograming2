package ce.mnu.siteuser;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@Getter @Setter
public class FileDto {
	private String fileName;
	private String contentType;
}
