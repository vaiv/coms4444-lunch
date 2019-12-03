import numpy as np
import cv2


def main():
    grid = np.zeros((1001, 1001, 3))
    grid += 255

    for x in range(0, 1001, 10):
        f = x-1 if x > 0 else x
        grid[f:x+2, :] = (0, 0, 0)

    for y in range(0, 1001, 10):
        f = y-1 if y > 0 else y
        grid[:, f:y+2] = (0, 0, 0)

    for x in range(0, 1001, 100):
        f = x-1 if x > 0 else x
        grid[f:x+2, :] = (0, 0, 255)

    for y in range(0, 1001, 100):
        f = y-1 if y > 0 else y
        grid[:, f:y+2] = (0, 0, 255)

    cv2.imwrite('grid.png', grid)


if __name__ == '__main__':
    main()
