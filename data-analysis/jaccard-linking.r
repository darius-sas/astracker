args <- commandArgs(TRUE)

if (length(args) < 2) {
  write("Usage: Rscript jaccard-linking.r <similarity-score-file.csv> <output-file.ext>", stdout())
  quit()
}

draw_similarity_score_plots <- function(data_file, base_size = 12){
  library(ggplot2)
  library(stringr)
  library(gridExtra)
  
  df <- read.csv(data_file)
  df$curId <- as.character(df$curId)
  df$nextId <- as.character(df$nextId)
  
  plots <- c()
  
  versions = levels(df$currentVersion);
  
  for (i in 1:(length(versions)-1)) {
    current_version = versions[i]
    next_version = versions[i+1]
    
    df.curr <- df[df$currentVersion == current_version & df$nextVersion == next_version,]
    
    ytitle = paste("Next version", next_version, "smell ID")
    xtitle = paste("Current version", current_version, "smell ID")
    
    # There is a diagonal of because the plotting function orders the data and arcan 
    # detects the smells in a similar order from version to version
    p <- ggplot(df.curr, aes(curId, nextId)) +
      geom_tile(aes(fill = similarityScore), colour = "gray10") + 
      scale_fill_gradient(low = "white", high = "firebrick") +
      theme_gray(base_size = base_size) +
      labs(x = xtitle, y = ytitle) +
      scale_x_discrete(expand = c(0,0)) + scale_y_discrete(expand = c(0,0)) +
      theme(#axis.ticks = element_blank(),
            axis.text.x = element_text(size = base_size *.8, angle = 90),
            axis.text.y = element_text(size = base_size *.6)) +
      geom_point(data = subset(df.curr, matched == "true"), colour="cyan3", shape=16, size=2, na.rm = T)
    
    plots[[i]] <- p
  }
  return(plots)
}

# TODO complete plotting, check version 3.0 antlr what's happening

fin <- args[1]
fout <- args[2]

plots <- draw_similarity_score_plots(fin)
pdf(fout, width = 20, height = 15)
invisible(lapply(plots, print))
dev.off()
# https://stackoverflow.com/questions/20500706/saving-multiple-ggplots-from-ls-into-one-and-separate-files-in-r
#do.call(grid.arrange, args=c(plots, ncol=1))


