clear ; close all; clc
debug_on_warning(1)
warning ("off", "Octave:broadcast");

load('idx_min.mat')
K_max = size(J_min_vector, 1)

for K = 1:K_max
	centroids = computeCentroids(X, idx_min_matrix(:, K), K)
	fprintf('\n')
end
