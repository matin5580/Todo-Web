package web.todo.ToDoWeb.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.todo.ToDoWeb.exception.DoplicateException;
import web.todo.ToDoWeb.exception.EmptyException;
import web.todo.ToDoWeb.exception.NotFoundException;
import web.todo.ToDoWeb.model.ToDo;
import web.todo.ToDoWeb.model.ToDoList;
import web.todo.ToDoWeb.model.User;
import web.todo.ToDoWeb.repository.UserRepository;
import web.todo.ToDoWeb.service.FilledValidation;
import web.todo.ToDoWeb.service.ListService;

@Service
public class ListServiceImpl extends BaseServiceImpl<User, String, UserRepository> implements ListService, FilledValidation {

    private final UserRepository userRepository;

    @Autowired
    public ListServiceImpl(UserRepository repository, UserRepository userRepository) {
        super(repository);
        this.userRepository = userRepository;
    }

    @Override
    public User addListToFolder(String folderName, String listName, String username) {
        notEmptyAssertion(folderName);
        notEmptyAssertion(listName);
        notEmptyAssertion(username);

        existsByListNameAssertion(listName, folderName, username);

        if (userRepository.findByToDoFoldersNameAndIdAndIsDeletedFalse(folderName, username).isPresent()){
            User user = userRepository.findByToDoFoldersNameAndIdAndIsDeletedFalse(folderName, username).get();
            ToDoList toDoList = new ToDoList();
            toDoList.setName(listName);
            user.getToDoFolders().stream().filter(folder -> folder.getName().equals(folderName)).forEach(folder -> folder.getToDoLists().add(toDoList));
            return save(user);
        }else {
            throw new NotFoundException("The username or folder name provided is wrong");
        }
    }


    @Override
    public void changeListName(String oldListName, String newListName, String folderName, String userId) {
        notEmptyAssertion(oldListName);
        notEmptyAssertion(newListName);
        notEmptyAssertion(folderName);
        notEmptyAssertion(userId);

        notExistByListNameAssertion(oldListName, folderName, userId);

        existsByListNameAssertion(newListName, folderName, userId);

        if (userRepository.findByToDoFoldersNameAndIdAndIsDeletedFalse(folderName, userId).isPresent()){
            User user = userRepository.findByToDoFoldersNameAndIdAndIsDeletedFalse(folderName, userId).get();
            user.getToDoFolders().stream().filter(folder -> folder.getName().equals(folderName)).forEach( folder -> folder.getToDoLists().stream().filter(list -> list.getName().equals(oldListName)).forEach(list -> list.setName(newListName)));
            save(user);
        }else {
            throw new NotFoundException("The folder name or username is wrong");
        }
    }



    @Override
    public void deleteList(String listName, String folderName, String userId) {
        notEmptyAssertion(listName);
        notEmptyAssertion(folderName);
        notEmptyAssertion(userId);

        notExistByListNameAssertion(listName, folderName, userId);

        if (userRepository.findByToDoFoldersNameAndIdAndIsDeletedFalse(folderName, userId).isPresent()){
            User user = userRepository.findByToDoFoldersNameAndIdAndIsDeletedFalse(folderName, userId).get();
            user.getToDoFolders().stream().filter(folder -> folder.getName().equals(folderName)).forEach(folder -> folder.getToDoLists().removeIf(toDoList -> toDoList.getName().equals(listName)));
            save(user);
        } else {
            throw new NotFoundException("The folder name or username is wrong");
        }
    }

    @Override
    public void insertToDoToList(ToDo toDo, String listName, String folderName, String userId) {
        notEmptyAssertion(listName);
        notEmptyAssertion(folderName);
        notEmptyAssertion(userId);

        notExistByListNameAssertion(listName, folderName, userId);

        if (userRepository.findByToDoFoldersNameAndIdAndIsDeletedFalse(folderName, userId).isPresent()){
            User user = userRepository.findByToDoFoldersNameAndIdAndIsDeletedFalse(folderName, userId).get();
            user.getToDoFolders().stream().filter(folder -> folder.getName().equals(folderName)).forEach(folder -> folder.getToDoLists().stream().filter(toDoList -> toDoList.getName().equals(listName)).forEach(list -> list.getToDos().add(toDo)));
            save(user);
        } else {
            throw new NotFoundException("The folder name or username is wrong");
        }
    }


    @Override
    public void removeToDoFromList(String folderName, String listName, String userId, String toDoId) {
        notEmptyAssertion(folderName);
        notEmptyAssertion(listName);
        notEmptyAssertion(userId);
        notEmptyAssertion(toDoId);

        notExistByListNameAssertion(listName, folderName, userId);

        if (userRepository.findByToDoFoldersNameAndIdAndIsDeletedFalse(folderName, userId).isPresent()){
            User user = userRepository.findByToDoFoldersNameAndIdAndIsDeletedFalse(folderName, userId).get();
            user.getToDoFolders()
                    .stream()
                    .filter(folder -> folder.getName().equals(folderName))
                    .forEach(folder -> folder.getToDoLists()
                            .stream()
                            .filter(toDoList -> toDoList.getName().equals(listName))
                            .forEach(toDoList -> toDoList.getToDos()
                                    .removeIf(toDo -> toDo.getId().equals(toDoId))));
            save(user);
        } else {
            throw new NotFoundException("The folder name or username is wrong");
        }
    }

    @Override
    public Boolean existsByToDoListName(String toDoListName, String toDoFolderName, String userId) {
        return userRepository.existsByToDoFoldersToDoListsNameAndToDoFoldersNameAndIdAndIsDeletedFalse(toDoListName, toDoFolderName, userId) != null;
    }

    private void existsByListNameAssertion(String newListName, String folderName, String username) {
        if (existsByToDoListName(newListName, folderName, username)) {
            throw new DoplicateException("The list with the same name already exists");
        }
    }

    @Override
    public void notEmptyAssertion(String attribute){
        if (isNull(attribute) || isBlank(attribute) || isWhiteSpace(attribute) || isEmpty(attribute)) {
            throw new EmptyException("The password provided is empty");
        }
    }

    private void notExistByListNameAssertion(String listName, String folderName, String username) {
        if (!existsByToDoListName(listName, folderName, username)) {
            throw new EmptyException("The list name provided doesn't belong to user");
        }
    }

    @Override
    public Boolean isEmpty(String field) {
        return field.isEmpty();
    }

    @Override
    public Boolean isBlank(String field) {
        return field.isBlank();
    }

    @Override
    public Boolean isNull(String field) {
        return field == null;
    }

    @Override
    public Boolean isWhiteSpace(String field) {
        return field.trim().isEmpty();
    }
}
