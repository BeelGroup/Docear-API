clear ; close all; clc
warning ("off", "Octave:broadcast");

load('user_data.mat')
load('idx_min.mat')

% X: original user data
% idx_min: m x K_max size matrix with the indices of the users in X that minimize the cost function J
% J_min: K_max size vector with the minimum cost for the index K

plotData(X, idx_min, K)
