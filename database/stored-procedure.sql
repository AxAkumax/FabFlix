DELIMITER //
CREATE PROCEDURE add_movie (
    IN movie_id VARCHAR(10),
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(32)
)
BEGIN
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;

    -- Check if the star exists, if not, insert it
    SELECT id INTO star_id FROM stars WHERE name = star_name;
    IF star_id IS NULL THEN
        INSERT INTO stars (name) VALUES (star_name);
        SET star_id = LAST_INSERT_ID();
    END IF;

    -- Check if the genre exists, if not, insert it
    SELECT id INTO genre_id FROM genres WHERE name = genre_name;
    IF genre_id IS NULL THEN
        INSERT INTO genres (name) VALUES (genre_name);
        SET genre_id = LAST_INSERT_ID();
    END IF;

    -- Insert the movie
    INSERT INTO movies (id, title, year, director) VALUES (movie_id, movie_title, movie_year, movie_director);

    -- Link the star to the movie
    INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);

    -- Link the genre to the movie
    INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);
END //

DELIMITER ;
