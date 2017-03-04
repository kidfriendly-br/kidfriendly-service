package com.holictechnology.kidfriendly.ejbs;


import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.core.Response.Status;

import com.holictechnology.kidfriendly.domain.dtos.LoginDto;
import com.holictechnology.kidfriendly.domain.dtos.MessageDto;
import com.holictechnology.kidfriendly.domain.entitys.Login;
import com.holictechnology.kidfriendly.domain.entitys.User;
import com.holictechnology.kidfriendly.ejbs.interfaces.LoginLocal;
import com.holictechnology.kidfriendly.library.exceptions.KidFriendlyException;
import com.holictechnology.kidfriendly.library.messages.KidFriendlyMessages;
import com.holictechnology.kidfriendly.library.utilites.CriptographUtilites;
import com.holictechnology.kidfriendly.mount.dto.LoginToLoginDto;


@Stateless
public class LoginEJB extends AbstractEJB implements LoginLocal {

    private static final long serialVersionUID = 8439680343365480598L;

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(value = TxType.NOT_SUPPORTED)
    public List<LoginDto> returnLoginAdm(String search) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT l FROM Login l WHERE l.stActive = :stActive AND l.user = null ");

        if (!search.equals("null"))
            sql.append(" 	AND l.idLogin like :idLogin ");

        sql.append(" 		ORDER  BY l.idLogin ");

        Query query = entityManager.createQuery(sql.toString());
        query.setParameter("stActive", Boolean.FALSE);

        if (!search.equals("null"))
            query.setParameter("idLogin", "%" + search + "%");

        return LoginToLoginDto.getInstance().loginToLoginDto(query.getResultList());
    }

    @Override
    public LoginDto saveOrAlterUserAdm(LoginDto loginDto) {
        Login login = LoginToLoginDto.getInstance().loginDtoToLoginDtoSingle(loginDto);

        entityManager.merge(login);

        return LoginToLoginDto.getInstance().loginToLoginDtoSingle(login);
    }

    @Override
    public MessageDto deleteUserAdm(String idLogin) {
        MessageDto message = new MessageDto();

        try {
            Login login = entityManager.find(Login.class, idLogin);
            entityManager.remove(login);
            message.setMessage("Removido com sucesso.");
            message.setStatusSuccess(true);
        } catch (Exception e) {
            message.setMessage("Erro ao remover usuário, tente mais tarde e se persistir entre em contato com o suporte.");
            message.setStatusSuccess(false);
        }

        return message;
    }

    @Override
    public LoginDto registerUserAdm(String user, String pws) {
        Login login = new Login();

        login.setIdLogin(user);
        login.setDesPassword(pws);
        login.setStActive(Boolean.FALSE);

        entityManager.persist(login);

        return LoginToLoginDto.getInstance().loginToLoginDtoSingle(login);
    }

    @Override
    public Login login(String user, String pws) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT l FROM Login l WHERE l.stActive = :stActive AND l.user = null ");
        sql.append(" 	AND l.idLogin = :idLogin ");
        sql.append(" 	AND l.desPassword = :desPassword ");

        Query query = entityManager.createQuery(sql.toString());
        query.setParameter("stActive", Boolean.FALSE);
        query.setParameter("idLogin", user);
        query.setParameter("desPassword", pws);

        try {
            Login login = (Login) query.getSingleResult();
            return login;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.holictechnology.kidfriendly.ejbs.interfaces.LoginLocal#
     * authenticateUser(java.lang.String)
     */
    @Override
    @Transactional(value = TxType.NOT_SUPPORTED)
    public User authenticateUser(String email) throws KidFriendlyException {
        User user = null;

        try {
            StringBuffer hql = new StringBuffer();
            hql.append("SELECT user ");
            hql.append("FROM com.holictechnology.kidfriendly.domain.entitys.User AS user ");
            hql.append("INNER JOIN FETCH user.login AS login ");
            hql.append("WHERE login.idLogin LIKE :email ");
            TypedQuery<User> typedQuery = entityManager.createQuery(hql.toString(), User.class);
            typedQuery.setParameter("email", email);
            user = typedQuery.getSingleResult();
            user.getLogin().setDesPassword(CriptographUtilites.getInstance().createToken(email.concat(user.getLogin().getDesPassword())));
        } catch (NoResultException e) {
            throw new KidFriendlyException(Status.NOT_FOUND, KidFriendlyMessages.ERROR_AUTHENTICATE_LOGIN_NOT_FOUND);
        } catch (NoSuchAlgorithmException e) {
            throw new KidFriendlyException(KidFriendlyMessages.ERROR_AUTHENTICATE_CREATE_TOKEN);
        }

        return user;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.holictechnology.kidfriendly.ejbs.interfaces.LoginLocal#
     * authenticateSocialNetwork(java.lang.Long)
     */
    @Override
    @Transactional(value = TxType.NOT_SUPPORTED)
    public User authenticateSocialNetwork(Long idSocialNetwork) throws KidFriendlyException {
        User user = null;

        try {
            StringBuffer hql = new StringBuffer();
            hql.append("SELECT user ");
            hql.append("FROM com.holictechnology.kidfriendly.domain.entitys.User AS user ");
            hql.append("WHERE user.idSocialNetwork = :idSocialNetwork ");
            TypedQuery<User> typedQuery = entityManager.createQuery(hql.toString(), User.class);
            typedQuery.setParameter("idSocialNetwork", idSocialNetwork);
            user = typedQuery.getSingleResult();
        } catch (NoResultException e) {}

        return user;
    }
}
