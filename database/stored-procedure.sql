DELIMITER //

CREATE PROCEDURE add_movie (
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(32)
)
BEGIN
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;
    DECLARE movie_id VARCHAR(10);
    DECLARE movie_id_exists INT;

    -- Check if the movie already exists (based on title and year)
    SELECT COUNT(*) INTO movie_id_exists FROM movies WHERE title = movie_title AND year = movie_year;

    IF movie_id_exists > 0 THEN
        -- Movie already exists, return without adding
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Movie already exists';
    ELSE
        -- Check if the star exists, if not, insert it
        SELECT id INTO star_id FROM stars WHERE name = star_name;

        IF star_id IS NULL THEN
            -- Generate a UUID for the star
            SET star_id = UUID();
            -- If the star doesn't exist, insert it
            INSERT INTO stars (id, name) VALUES (star_id, star_name);
        END IF;

        -- Check if the genre exists, if not, insert it
        SELECT id INTO genre_id FROM genres WHERE name = genre_name;

        IF genre_id IS NULL THEN
            -- If the genre doesn't exist, insert it
            INSERT INTO genres (name) VALUES (genre_name);
            SET genre_id = LAST_INSERT_ID();
        END IF;

        -- Generate UUID for the movie id
        SET movie_id = UUID();

        -- Insert the movie
        INSERT INTO movies (id, title, year, director) VALUES (movie_id, movie_title, movie_year, movie_director);

        -- Link the star to the movie
        INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);

        -- Link the genre to the movie
        INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);
    END IF;
END //

DELIMITER ;

