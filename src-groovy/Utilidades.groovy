import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.naming.directory.SearchResult
import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.APIClient
import org.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.engine.api.ProfileAPI
import org.bonitasoft.engine.profile.Profile
import org.bonitasoft.engine.profile.ProfileMemberCreator
import org.bonitasoft.engine.profile.ProfileSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.search.impl.SearchResultImpl
import groovy.json.JsonBuilder
import org.bonitasoft.engine.identity.Role
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.identity.Group
import org.bonitasoft.engine.identity.UserCreator
import org.bonitasoft.engine.identity.GroupSearchDescriptor
import org.bonitasoft.engine.identity.UserSearchDescriptor

class Utilidades {
	public Long obtenerIDPerfil(APIAccessor apiAccessor, String perfil) {
		SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0,100);
		searchOptionsBuilder.filter(ProfileSearchDescriptor.NAME, perfil);
		SearchResultImpl<Profile> searchResultProfile = apiAccessor.profileAPI.searchProfiles(searchOptionsBuilder.done());
		for (var in searchResultProfile.getResult()) {
			return var.id;
		}
	}
	
	public Long obtenerIDGrupo(APIAccessor apiAccessor, String grupo) {
		SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
		searchOptionsBuilder.filter(GroupSearchDescriptor.NAME, grupo);
		SearchResultImpl<Group> searchResultGroup = apiAccessor.identityAPI.searchGroups(searchOptionsBuilder.done());
		for (var in searchResultGroup.getResult()) {
			return var.id;
		}
	}
	
	public Long obtenerIDRol(APIAccessor apiAccessor, String rol) {
		SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0,100);
		searchOptionsBuilder.filter(GroupSearchDescriptor.NAME, rol);
		SearchResultImpl<Role> searchResultRole = apiAccessor.identityAPI.searchRoles(searchOptionsBuilder.done());
		for (var in searchResultRole.getResult()) {
			 return var.id;
		}
					
	}
	
	public User crearUsuario(APIAccessor apiAccessor, String usuario, String nombre, String apellido, String clave, Long idPerfil) {
		UserCreator userCreator = new UserCreator(usuario, clave);
		userCreator.setFirstName(nombre).setLastName(apellido);
		User user = apiAccessor.identityAPI.createUser(userCreator);
		ProfileMemberCreator profileMemberCreator = new ProfileMemberCreator(idPerfil);
		profileMemberCreator.setUserId(user.getId());
		apiAccessor.profileAPI.createProfileMember(profileMemberCreator);
		return user;
	}
	
	public void crearMembresia(APIAccessor apiAccessor, User user, Long idGrupo, Long idRol) {
		try {
			apiAccessor.identityAPI.addUserMembership(user.getId(), idGrupo, idRol);
		}catch(Exception ex) {
			print(ex);
		}
	}
	
	public String crearContraseña(Integer caracteres) {
		String letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder stringBuilder = new StringBuilder();
		Random random = new Random();
		for(int i = 0; i < caracteres; i++) {
				stringBuilder.append(letras.charAt(random.nextInt(letras.length())));
		}
		return stringBuilder.toString();
	}
	
	public SearchResultImpl<User> buscarXRoles(APIAccessor apiAccessor, Long idGrupo, Long idRol){
		IdentityAPI identityAPI = apiAccessor.getIdentityAPI();
		SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
		searchOptionsBuilder.filter(UserSearchDescriptor.GROUP_ID, idGrupo);
		searchOptionsBuilder.filter(UserSearchDescriptor.ROLE_ID, idRol);
		SearchResultImpl<User> userResults = identityAPI.searchUsers(searchOptionsBuilder.done());
	}
	
	public List enlistarCSV(APIAccessor apiAccessor, String idStorage) {
		List usuarios=[];
		BufferedReader bufferedReader;
		String nombres="";
		String apellidos="";
		String correo="";
		try {
		   bufferedReader =new BufferedReader(new InputStreamReader(new ByteArrayInputStream(apiAccessor.processAPI.getDocumentContent(idStorage))));
		   String fila = bufferedReader.readLine();
		   while (fila) {
			  List datos = fila.split(",") as List;
			  nombres = datos[0].toString();
			  apellidos = datos[1].toString();
			  correo = datos[2].toString();
			  if(correoValido(correo)) {
				  usuarios.add(nombre:nombres, apellido:apellidos, correo:correo);
			  }
			  fila = bufferedReader.readLine();
		   }
		} catch (Exception e) {
		   print(e);
		} finally {
		   if (bufferedReader) {
			  bufferedReader.close();
		   }
		}
		return usuarios;
	}
	
	public Boolean correoValido(String correo) {
		Pattern pattern = Pattern.compile("([a-z0-9]+(\\.?[a-z0-9])*)+@(([a-z]+)\\.([a-z]+))+");
		Matcher mather = pattern.matcher(correo);
		if (mather.find()) {
			return true;
		}else {
			return false;
		}
	}
}
