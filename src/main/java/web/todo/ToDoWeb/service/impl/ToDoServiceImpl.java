package web.todo.ToDoWeb.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import web.todo.ToDoWeb.exception.EmptyException;
import web.todo.ToDoWeb.model.ToDo;
import web.todo.ToDoWeb.model.User;
import web.todo.ToDoWeb.repository.ToDoRepository;
import web.todo.ToDoWeb.service.CategoryFactory;
import web.todo.ToDoWeb.service.FilledValidation;
import web.todo.ToDoWeb.service.ListService;
import web.todo.ToDoWeb.service.ToDoService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import static org.springframework.http.MediaType.*;
import static web.todo.ToDoWeb.constants.FileConstants.*;

@Service
public class ToDoServiceImpl extends BaseServiceImpl<ToDo, String, ToDoRepository> implements ToDoService, FilledValidation {

    private final ToDoRepository toDoRepository;
    private final ListService listService;
    private final CategoryFactory categoryFactory;

    @Autowired
    public ToDoServiceImpl(ToDoRepository repository, ToDoRepository toDoRepository, ListService listService, CategoryFactory categoryFactory) {
        super(repository);
        this.toDoRepository = toDoRepository;
        this.listService = listService;
        this.categoryFactory = categoryFactory;
    }

    @Override
    public void saveToDoInList(ToDo toDo, String listName, String folderName, String username) {
        if (isEmpty(toDo.getTask()) || isBlank(toDo.getTask()) || isNull(toDo.getTask()) || isWhiteSpace(toDo.getTask())){
            throw new EmptyException("For to do at least you should fill task");
        }
        ToDo savedToDo = save(toDo);
        listService.insertToDoToList(savedToDo,listName,folderName,username);
    }

    @Override
    public void updateToDo(ToDo toDo) {
        if (isEmpty(toDo.getTask()) || isBlank(toDo.getTask()) || isNull(toDo.getTask()) || isWhiteSpace(toDo.getTask())){
            throw new EmptyException("For to do at least you should fill task");
        }
        if (isEmpty(toDo.getId()) || isBlank(toDo.getId()) || isNull(toDo.getId()) || isWhiteSpace(toDo.getId())){
            throw new EmptyException("The to do must have id");
        }
        save(toDo);
    }

    @Override
    public void deleteToDo(String folderName, String listName, String userName, String toDoId) {
        if (isEmpty(toDoId) || isBlank(toDoId) || isNull(toDoId) || isWhiteSpace(toDoId)){
            throw new EmptyException("The id field is empty");
        }
        listService.removeToDoFromList(folderName, listName, userName, toDoId);
        deleteToDoPictures(toDoId);
        deleteById(toDoId);
    }

    @Override
    public void deleteToDoPictures(String toDoId) {
        File file = new File(TODO_FOLDER + toDoId);
        for (File subFile: Objects.requireNonNull(file.listFiles())){
            subFile.delete();
        }
        file.delete();
    }

    @Override
    public void saveToDoInCategory(ToDo toDo, User user) {
        if (isEmpty(toDo.getTask()) || isBlank(toDo.getTask()) || isNull(toDo.getTask()) || isWhiteSpace(toDo.getTask())){
            throw new EmptyException("For to do at least you should fill task");
        }
        ToDo savedToDo = save(toDo);
    }

    @Override
    public void addPhoto(String toDoId, MultipartFile picture) throws IOException {
        ToDo toDo = findById(toDoId).get();
        if (picture != null) {
            if (!Arrays.asList(IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE, IMAGE_GIF_VALUE).contains(picture.getContentType())){
                throw new IllegalStateException("Type of file is invalid");
            }
            Path toDoFolder = Paths.get(TODO_FOLDER + toDoId).toAbsolutePath().normalize();
            if (!Files.exists(toDoFolder)){
                Files.createDirectories(toDoFolder);
            }
            Files.copy(picture.getInputStream(), toDoFolder.resolve(picture.getOriginalFilename()));
            toDo.getPictures().add(setPictureImageUrl(toDoId,picture));
            save(toDo);
        }
    }



    @Override
    public void deleteToDoPicture(String toDoId, String pictureName) throws IOException {
        Path pictureDirectory = Paths.get(TODO_FOLDER + toDoId + FORWARD_SLASH + pictureName).toAbsolutePath().normalize();
        Files.deleteIfExists(pictureDirectory);
        ToDo toDo = findById(toDoId).get();
        toDo.getPictures().removeIf(element -> element.equals(ServletUriComponentsBuilder.fromCurrentContextPath().path(TODO_IMAGE_PATH + toDoId + FORWARD_SLASH + pictureName).toUriString()));
        save(toDo);
    }

    private String setPictureImageUrl(String toDoId, MultipartFile picture) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(TODO_IMAGE_PATH + toDoId + "/" + picture.getOriginalFilename()).toUriString();
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

