package com.user.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@SpringBootApplication
public class LoginApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoginApplication.class, args);
	}

	@GetMapping
	public String app() {
		return "/login";
	}

//	@PostMapping("/login/oauth2/code/google")
//	public String redirectURI(@RequestBody GoogleOAuth2UserInfo userRequest) {
//
//		System.out.println(userRequest.getEmail());
//		System.out.println(userRequest.getName());
//
//		return "OK :)";
//	}

	@GetMapping("/fail")
	public String fail() {
		return "FAIL";
	}

//	@GetMapping("/success")
//	public String success() {
//		return "success !!!";
//	}


	//
	@Autowired
	private OAuth2AuthorizedClientService authorizedClientService;


	/* HAY QUE CONECTAR CON EL FRONTEND Y QUE EL FRONTEND NOS ENVIE EL TOKEN DE GOOGLE. 
	 * Este método se llama luego de que se haya conectado con google,
	 * talvez para que nos mande el frontend el token de google haya quq usar
	 * el @RequestBody */
	@GetMapping("/loginSuccess")
	public String getLoginInfo(/*@RequestBody*/ OAuth2AuthenticationToken authentication) {

		
		OAuth2AuthorizedClient client = authorizedClientService
				.loadAuthorizedClient(
						authentication.getAuthorizedClientRegistrationId(),
						authentication.getName());
		System.out.println("AUTENTICATION NAME: "+authentication.getName());

		String userInfoEndpointUri = client.getClientRegistration()
				.getProviderDetails().getUserInfoEndpoint().getUri();

		if (!StringUtils.isEmpty(userInfoEndpointUri)) {
			
			//Hace la peticion a google
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken()
					.getTokenValue());

			System.out.println("TOKEN: "+client.getAccessToken()
					.getTokenValue());

			HttpEntity entity = new HttpEntity("", headers);
			ResponseEntity<Map> response = restTemplate
					.exchange(userInfoEndpointUri, HttpMethod.GET, entity, Map.class);
			//Obtiene la respuesta de google y los guarda en un MAP<>, del cual se puede obtener los datos
			Map userAttributes = response.getBody();
			System.out.println(userAttributes.get("name"));

			//imprime todos los atributos que google nos trae
			for (Object key: userAttributes.keySet()){
				System.out.println(key+ " = " + userAttributes.get(key));
			}

			/*valida que el correo sea @tecazuay.edu.ec, se puede validar con ".est@tecazuay.edu.ec"
			 *para estudiantes.
			 */
			if (!((String)userAttributes.get("email")).contains(".est@tecazuay.edu.ec")) {
				//throw  new RuntimeException("No pertenece al ISTA");
				return "Seleccione un correo del ISTA";
			}
		}

		/* Se puede retornar un boolean, o el tipo de dato necesario */
		return "loginSuccess";
	}


}
